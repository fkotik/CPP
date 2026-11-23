package ru.cpp.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cpp.backend.dto.WeighingCalcDto;
import ru.cpp.backend.dto.WeighingCreateRequest;
import ru.cpp.backend.dto.WeighingDto;
import ru.cpp.backend.entity.AppUser;
import ru.cpp.backend.entity.Batch;
import ru.cpp.backend.entity.BatchStatus;
import ru.cpp.backend.entity.ControlWeighing;
import ru.cpp.backend.exception.ApiException;
import ru.cpp.backend.repository.AppUserRepository;
import ru.cpp.backend.repository.BatchRepository;
import ru.cpp.backend.repository.ControlWeighingRepository;
import ru.cpp.backend.repository.PassportRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class WeighingService {

    private static final Set<BatchStatus> ALLOWED_STATUSES =
            EnumSet.of(BatchStatus.GROWING, BatchStatus.WEIGHING);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ControlWeighingRepository weighingRepository;
    private final BatchRepository batchRepository;
    private final AppUserRepository userRepository;
    private final GrowthNormService growthNormService;

    public WeighingService(ControlWeighingRepository weighingRepository,
                           BatchRepository batchRepository,
                           AppUserRepository userRepository,
                           GrowthNormService growthNormService) {
        this.weighingRepository = weighingRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.growthNormService = growthNormService;
    }

    @Transactional(readOnly = true)
    public List<WeighingDto> list(Integer batchId) {
        requireBatch(batchId);
        return weighingRepository.findByBatch_IdOrderByWeighingDateAscIdAsc(batchId)
                .stream()
                .map(WeighingDto::from)
                .toList();
    }

    /** Только считает, ничего не сохраняет: для "живого" расчёта в форме. */
    @Transactional(readOnly = true)
    public WeighingCalcDto preview(Integer batchId, LocalDate weighingDate, BigDecimal actualAvgWeight) {
        return calculate(findBatch(batchId), weighingDate, actualAvgWeight);
    }

    @Transactional
    public WeighingDto create(Integer batchId, WeighingCreateRequest req, String login) {
        AppUser user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        Batch batch = findBatch(batchId);

        if (!ALLOWED_STATUSES.contains(batch.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Взвешивание доступно в статусах «Выращивание» и «Взвешивание», сейчас партия в статусе «"
                            + batch.getStatus().getLabel() + "»");
        }
        if (req.sampleSize() > batch.getInitialCount()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Размер выборки больше поголовья партии (" + batch.getInitialCount() + ")");
        }

        // Те же вычисления, что и в preview: результат на экране = результат в БД
        WeighingCalcDto calc = calculate(batch, req.weighingDate(), req.actualAvgWeight());

        ControlWeighing w = new ControlWeighing();
        w.setBatch(batch);
        w.setWeighingDate(req.weighingDate());
        w.setAgeDays(calc.ageDays());
        w.setSampleSize(req.sampleSize());
        w.setActualAvgWeight(req.actualAvgWeight().setScale(2, RoundingMode.HALF_UP));
        w.setPlannedAvgWeight(calc.plannedAvgWeight());
        w.setDeviationAbsolute(calc.deviationAbsolute());
        w.setDeviationPercent(calc.deviationPercent());
        w.setResponsible(user);

        weighingRepository.save(w);
        return WeighingDto.from(w);
    }

    // ------------------------------------------------------------------ расчёт

    private WeighingCalcDto calculate(Batch batch, LocalDate weighingDate, BigDecimal actual) {
        if (actual.signum() <= 0) {   // signum(): -1, 0 или 1 - знак числа
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Средний вес должен быть больше нуля");
        }
        if (weighingDate.isBefore(batch.getPlantingDate())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Дата взвешивания раньше даты посадки (" + batch.getPlantingDate() + ")");
        }
        if (weighingDate.isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Дата взвешивания не может быть в будущем");
        }

        int age = (int) ChronoUnit.DAYS.between(batch.getPlantingDate(), weighingDate);
        BigDecimal planned = growthNormService.plannedWeight(batch.getCrossBreed(), age);

        BigDecimal deviation = actual.subtract(planned).setScale(2, RoundingMode.HALF_UP);
        BigDecimal percent = deviation.multiply(HUNDRED).divide(planned, 2, RoundingMode.HALF_UP);
        // compareTo, а не equals: у BigDecimal 5.0 и 5.00 для equals - разные числа
        boolean alert = percent.abs().compareTo(PassportRepository.ALERT_PERCENT) > 0;

        return new WeighingCalcDto(age, planned, deviation, percent, alert);
    }

    private Batch findBatch(Integer id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Партия не найдена"));
    }

    private void requireBatch(Integer id) {
        if (!batchRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Партия не найдена");
        }
    }
}