package ru.cpp.backend.dto;

import ru.cpp.backend.entity.Batch;

import java.time.LocalDate;

/** Строка таблицы "Список партий". */
public record BatchListItemDto(
        Integer id,
        String batchNumber,
        String houseName,
        String crossBreed,
        LocalDate plantingDate,
        int initialCount,
        String status,        // код: PLANTING...
        String statusLabel,   // подпись: "Посадка"...
        LocalDate plannedSlaughterDate
) {
    public static BatchListItemDto from(Batch b) {
        return new BatchListItemDto(
                b.getId(),
                b.getBatchNumber(),
                b.getHouse().getName(),
                b.getCrossBreed(),
                b.getPlantingDate(),
                b.getInitialCount(),
                b.getStatus().name(),
                b.getStatus().getLabel(),
                b.getPlannedSlaughterDate());
    }
}