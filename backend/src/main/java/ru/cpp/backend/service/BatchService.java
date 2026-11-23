package ru.cpp.backend.service;

import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cpp.backend.dto.BatchCreateRequest;
import ru.cpp.backend.dto.BatchDetailDto;
import ru.cpp.backend.dto.BatchListItemDto;
import ru.cpp.backend.entity.AppUser;
import ru.cpp.backend.entity.Batch;
import ru.cpp.backend.entity.BatchStatus;
import ru.cpp.backend.entity.PoultryHouse;
import ru.cpp.backend.exception.ApiException;
import ru.cpp.backend.repository.AppUserRepository;
import ru.cpp.backend.repository.BatchRepository;
import ru.cpp.backend.repository.BatchSpecifications;
import ru.cpp.backend.repository.PoultryHouseRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BatchService {

    // С каких статусов технолог может переводить партию дальше
    private static final Set<BatchStatus> TECHNOLOGIST_CAN_ADVANCE_FROM =
            EnumSet.of(BatchStatus.PLANTING, BatchStatus.GROWING, BatchStatus.WEIGHING);

    private final BatchRepository batchRepository;
    private final PoultryHouseRepository houseRepository;
    private final AppUserRepository userRepository;

    public BatchService(BatchRepository batchRepository,
                        PoultryHouseRepository houseRepository,
                        AppUserRepository userRepository) {
        this.batchRepository = batchRepository;
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
    }

    // ------------------------------------------------------------------ создание

    @Transactional
    public BatchDetailDto create(BatchCreateRequest req, String login) {
        AppUser user = currentUser(login);

        PoultryHouse house = houseRepository.findById(req.houseId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Птичник не найден"));

        if (!CrossBreeds.ALL.contains(req.crossBreed())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Неизвестный кросс: " + req.crossBreed());
        }
        if (req.plannedSlaughterDate().isBefore(req.plantingDate())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Плановая дата убоя не может быть раньше даты посадки");
        }
        // Главная бизнес-проверка: поголовье не больше вместимости
        if (req.initialCount() > house.getCapacity()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, String.format(
                    "Поголовье (%d) превышает вместимость «%s» (%d)",
                    req.initialCount(), house.getName(), house.getCapacity()));
        }

        String number = req.autoNumber()
                ? generateNumber(req.plantingDate().getYear())
                : checkManualNumber(req.batchNumber());

        Batch batch = new Batch();
        batch.setBatchNumber(number);
        batch.setHouse(house);
        batch.setPlantingDate(req.plantingDate());
        batch.setInitialCount(req.initialCount());
        batch.setSourceSupplier(req.sourceSupplier().trim());
        batch.setCrossBreed(req.crossBreed());
        batch.setPlannedWeight(req.plannedWeight());
        batch.setPlannedSlaughterDate(req.plannedSlaughterDate());
        batch.setCreatedBy(user);
        // status остаётся по умолчанию PLANTING

        try {
            // saveAndFlush: INSERT выполняется сразу, а не при конце транзакции,
            // чтобы нарушение UNIQUE мы поймали здесь
            batchRepository.saveAndFlush(batch);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Номер партии «" + number + "» уже занят. Попробуйте ещё раз");
        }
        return toDetail(batch, user);
    }

    /** Следующий номер вида Б-2025-015: год посадки + порядковый номер внутри года. */
    private String generateNumber(int year) {
        String prefix = "Б-" + year + "-";
        int max = batchRepository.findNumbersByPrefix(prefix).stream()
                .map(n -> n.substring(prefix.length()))      // "Б-2025-014" -> "014"
                .filter(s -> s.matches("\\d{1,6}"))          // игнорируем ручные номера нестандартного вида
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        return prefix + String.format("%03d", max + 1);
    }

    private String checkManualNumber(String raw) {
        String number = raw == null ? "" : raw.trim();
        if (number.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Укажите номер партии или включите автоматическую нумерацию");
        }
        if (batchRepository.existsByBatchNumber(number)) {
            throw new ApiException(HttpStatus.CONFLICT, "Партия с номером «" + number + "» уже существует");
        }
        return number;
    }

    // ------------------------------------------------------------------ чтение

    @Transactional(readOnly = true)
    public List<BatchListItemDto> list(String search, Integer houseId, BatchStatus status,
                                       LocalDate plantedFrom, LocalDate plantedTo) {
        Sort sort = Sort.by(Sort.Order.desc("plantingDate"), Sort.Order.desc("id"));
        return batchRepository
                .findAll(BatchSpecifications.filter(search, houseId, status, plantedFrom, plantedTo), sort)
                .stream()
                .map(BatchListItemDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BatchDetailDto get(Integer id, String login) {
        return toDetail(findBatch(id), currentUser(login));
    }

    // ------------------------------------------------------------------ смена статуса

    @Transactional
    public BatchDetailDto advance(Integer id, String login) {
        AppUser user = currentUser(login);
        Batch batch = findBatch(id);

        BatchStatus next = batch.getStatus().next()
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Партия уже закрыта"));

        if (!canAdvance(user, batch.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Ваша роль не может переводить партию из статуса «" + batch.getStatus().getLabel() + "»");
        }
        // TODO (шаг про убой): при переходе SLAUGHTER -> CLOSED требовать наличие slaughter_result

        batch.setStatus(next);
        if (next == BatchStatus.CLOSED) {
            batch.setClosedAt(Instant.now());
        }
        // batchRepository.save(batch) НЕ нужен: сущность "управляемая", и Hibernate сам заметит
        // изменение полей и сделает UPDATE при коммите (dirty checking). В Eloquent нужен ->save().
        return toDetail(batch, user);
    }

    // ------------------------------------------------------------------ вспомогательное

    private AppUser currentUser(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    private Batch findBatch(Integer id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Партия не найдена"));
    }

    /** Может ли пользователь перевести партию, находящуюся СЕЙЧАС в статусе from. */
    private boolean canAdvance(AppUser user, BatchStatus from) {
        // switch-выражение (Java 14+): компактнее цепочки if / else
        return switch (user.getRole().getCode()) {
            case "TECHNOLOGIST" -> TECHNOLOGIST_CAN_ADVANCE_FROM.contains(from);
            case "QC" -> from == BatchStatus.SLAUGHTER;
            default -> false;
        };
    }

    private BatchDetailDto toDetail(Batch b, AppUser viewer) {
        Optional<BatchStatus> next = b.getStatus().next();

        // Возраст: до сегодня, а у закрытой партии - до даты закрытия
        LocalDate end = b.getClosedAt() != null
                ? b.getClosedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();
        int age = (int) Math.max(0, ChronoUnit.DAYS.between(b.getPlantingDate(), end));

        return new BatchDetailDto(
                b.getId(),
                b.getBatchNumber(),
                b.getHouse().getId(),
                b.getHouse().getName(),
                b.getHouse().getCapacity(),
                b.getPlantingDate(),
                b.getInitialCount(),
                b.getSourceSupplier(),
                b.getCrossBreed(),
                b.getPlannedWeight(),
                b.getPlannedSlaughterDate(),
                b.getStatus().name(),
                b.getStatus().getLabel(),
                next.map(Enum::name).orElse(null),
                next.map(BatchStatus::getLabel).orElse(null),
                next.isPresent() && canAdvance(viewer, b.getStatus()),
                age,
                b.getCreatedBy().getFullName(),
                b.getCreatedAt(),
                b.getClosedAt());
    }
}