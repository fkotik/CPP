package ru.cpp.backend.dto;

import java.math.BigDecimal;

/** Результат расчёта: используется и для предпросмотра, и при сохранении. */
public record WeighingCalcDto(
        int ageDays,
        BigDecimal plannedAvgWeight,
        BigDecimal deviationAbsolute,
        BigDecimal deviationPercent,
        boolean alert
) {
}