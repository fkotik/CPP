package ru.cpp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LatestWeighingDto(
        LocalDate weighingDate,
        int ageDays,
        BigDecimal actualAvgWeight,    // г
        BigDecimal plannedAvgWeight,   // г
        BigDecimal deviationPercent,
        boolean alert                  // |отклонение| > 5%
) {
}