package ru.cpp.backend.dto;

import java.math.BigDecimal;

/** Данные для карточек и красного баннера на странице партии. */
public record PassportSummaryDto(
        BigDecimal survivalPercent,        // null, пока нет результата убоя
        LatestWeighingDto latestWeighing   // null, если взвешиваний ещё не было
) {
}