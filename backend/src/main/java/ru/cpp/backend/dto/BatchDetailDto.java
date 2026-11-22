package ru.cpp.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Шапка карточки партии. Сохранность и отклонения добавим на следующих шагах. */
public record BatchDetailDto(
        Integer id,
        String batchNumber,
        Integer houseId,
        String houseName,
        int houseCapacity,
        LocalDate plantingDate,
        int initialCount,
        String sourceSupplier,
        String crossBreed,
        BigDecimal plannedWeight,          // граммы
        LocalDate plannedSlaughterDate,
        String status,
        String statusLabel,
        String nextStatus,                 // null, если партия закрыта
        String nextStatusLabel,
        boolean canAdvance,                // можно ли ТЕКУЩЕМУ пользователю нажать "Перевести в ..."
        int ageDays,
        String createdByName,
        Instant createdAt,
        Instant closedAt
) {
}