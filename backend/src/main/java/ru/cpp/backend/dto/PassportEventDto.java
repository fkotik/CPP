package ru.cpp.backend.dto;

import java.time.Instant;

/** Одна строка хронологии. */
public record PassportEventDto(
        String kind,          // INDICATOR, INCIDENT, VET, WEIGHING, SLAUGHTER (для фильтров и иконок)
        String kindLabel,     // "Показатель"...
        Integer id,
        Instant eventTime,
        String title,
        String details,
        String source,        // "SITUATION_CENTER" или "Ручной ввод"
        String responsible,   // null у автоматических событий
        boolean alert         // true: подсветить (отклонение, открытый инцидент, замечания)
) {
}