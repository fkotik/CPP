package ru.cpp.backend.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Что присылает форма "Создание партии". Аннотации проверяются при @Valid в контроллере.
 * Аналог правил в FormRequest. Вес - в граммах.
 */
public record BatchCreateRequest(

        boolean autoNumber,   // чекбокс "сформировать автоматически"

        @Size(max = 20, message = "Не длиннее 20 символов")
        String batchNumber,   // нужен, только если autoNumber = false

        @NotNull(message = "Выберите птичник")
        Integer houseId,

        @NotNull(message = "Укажите дату посадки")
        LocalDate plantingDate,

        @NotNull(message = "Укажите количество голов")
        @Min(value = 1, message = "Должно быть больше нуля")
        Integer initialCount,

        @NotBlank(message = "Укажите поставщика")
        @Size(max = 200, message = "Не длиннее 200 символов")
        String sourceSupplier,

        @NotBlank(message = "Выберите кросс")
        String crossBreed,

        @NotNull(message = "Укажите плановую массу")
        @DecimalMin(value = "1", message = "Не меньше 1 г")
        @DecimalMax(value = "10000", message = "Не больше 10000 г")
        BigDecimal plannedWeight,

        @NotNull(message = "Укажите плановую дату убоя")
        LocalDate plannedSlaughterDate
) {
}