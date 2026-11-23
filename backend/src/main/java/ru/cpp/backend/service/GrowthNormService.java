package ru.cpp.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cpp.backend.entity.GrowthNorm;
import ru.cpp.backend.exception.ApiException;
import ru.cpp.backend.repository.GrowthNormRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class GrowthNormService {

    private final GrowthNormRepository repository;

    public GrowthNormService(GrowthNormRepository repository) {
        this.repository = repository;
    }

    /**
     * Плановый средний вес (г) на заданный день жизни. В таблице точки раз в неделю,
     * между ними считаем по прямой (линейная интерполяция):
     *   вес = вес_нижней + (вес_верхней - вес_нижней) * (возраст - возраст_нижней) / (возраст_верхней - возраст_нижней)
     * Пример (Ross 308, 30 дней): 1590 + (2290 - 1590) * (30 - 28) / 7 = 1790.
     */
    @Transactional(readOnly = true)
    public BigDecimal plannedWeight(String crossBreed, int ageDays) {
        List<GrowthNorm> points = repository.findByCrossBreedOrderByAgeDaysAsc(crossBreed);
        if (points.size() < 2) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Для кросса «" + crossBreed + "» нет нормативов роста");
        }
        int maxAge = points.get(points.size() - 1).getAgeDays();
        if (ageDays < 0 || ageDays > maxAge) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, String.format(
                    "Нормативы заданы для возраста 0–%d дн., а возраст партии %d дн.", maxAge, ageDays));
        }

        // Ищем отрезок [lower; upper], в который попадает возраст
        for (int i = 1; i < points.size(); i++) {
            GrowthNorm lower = points.get(i - 1);
            GrowthNorm upper = points.get(i);
            if (ageDays <= upper.getAgeDays()) {
                int span = upper.getAgeDays() - lower.getAgeDays();
                int passed = ageDays - lower.getAgeDays();
                // У BigDecimal нет операторов + - * /: только методы.
                // divide(x, 2, HALF_UP) = "разделить, оставить 2 знака, округлить как в школе".
                BigDecimal delta = upper.getAvgWeightG().subtract(lower.getAvgWeightG())
                        .multiply(BigDecimal.valueOf(passed))
                        .divide(BigDecimal.valueOf(span), 2, RoundingMode.HALF_UP);
                return lower.getAvgWeightG().add(delta).setScale(2, RoundingMode.HALF_UP);
            }
        }
        // Сюда не дойти: возраст уже проверен на диапазон. Но компилятор требует return/throw.
        throw new IllegalStateException("Не найден отрезок нормативов");
    }
}