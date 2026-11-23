package ru.cpp.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Что вводит технолог. Возраст, план и отклонения он НЕ вводит: их считает сервер. */
public record WeighingCreateRequest(

        @NotNull(message = "Укажите дату взвешивания")
        LocalDate weighingDate,

        @NotNull(message = "Укажите размер выборки")
        @Min(value = 1, message = "Не меньше 1 головы")
        Integer sampleSize,

        @NotNull(message = "Укажите средний вес")
        @DecimalMin(value = "1", message = "Не меньше 1 г")
        @DecimalMax(value = "10000", message = "Не больше 10000 г")
        BigDecimal actualAvgWeight
) {
}