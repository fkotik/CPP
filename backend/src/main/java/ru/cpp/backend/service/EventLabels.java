package ru.cpp.backend.service;

import java.util.Map;

/**
 * Перевод кодов из БД в заголовки для интерфейса.
 * Неизвестный код показываем как есть (Ситуационный центр может прислать новый тип показателя).
 * Коды показателей должны совпадать с теми, что будет слать интеграция.
 */
final class EventLabels {

    private static final Map<String, String> INDICATORS = Map.of(
            "TEMPERATURE", "Температура",
            "HUMIDITY", "Влажность",
            "CO2", "Уровень CO₂",
            "AMMONIA", "Аммиак",
            "FEED_CONSUMPTION", "Расход корма",
            "WATER_CONSUMPTION", "Расход воды");

    private static final Map<String, String> VET = Map.of(
            "VACCINATION", "Вакцинация",
            "TREATMENT", "Лечение",
            "EXAMINATION", "Осмотр",
            "DIAGNOSTICS", "Диагностика");

    private static final Map<String, String> QUALITY = Map.of(
            "COMPLIANT", "соответствует",
            "WITH_REMARKS", "с замечаниями",
            "REJECT", "брак");

    private EventLabels() {
    }

    static String title(EventKind kind, String code) {
        // switch по enum без default: компилятор проверит, что перечислены ВСЕ значения
        return switch (kind) {
            case INDICATOR -> INDICATORS.getOrDefault(code, code);
            case INCIDENT -> code;                       // тип инцидента - свободный текст
            case VET -> VET.getOrDefault(code, code);
            case WEIGHING -> "Контрольное взвешивание";
            case SLAUGHTER -> "Убой: " + QUALITY.getOrDefault(code, code);
        };
    }
}