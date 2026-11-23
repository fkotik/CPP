package ru.cpp.backend.dto;

import ru.cpp.backend.entity.ControlWeighing;
import ru.cpp.backend.repository.PassportRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record WeighingDto(
        Integer id,
        LocalDate weighingDate,
        int ageDays,
        int sampleSize,
        BigDecimal actualAvgWeight,
        BigDecimal plannedAvgWeight,
        BigDecimal deviationAbsolute,
        BigDecimal deviationPercent,
        boolean alert,
        String responsible,
        Instant createdAt
) {
    public static WeighingDto from(ControlWeighing w) {
        return new WeighingDto(
                w.getId(),
                w.getWeighingDate(),
                w.getAgeDays(),
                w.getSampleSize(),
                w.getActualAvgWeight(),
                w.getPlannedAvgWeight(),
                w.getDeviationAbsolute(),
                w.getDeviationPercent(),
                w.getDeviationPercent().abs().compareTo(PassportRepository.ALERT_PERCENT) > 0,
                w.getResponsible().getFullName(),
                w.getCreatedAt());
    }
}