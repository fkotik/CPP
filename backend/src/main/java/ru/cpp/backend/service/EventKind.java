package ru.cpp.backend.service;

/** Тип записи в хронологии. Эти имена же приходят в параметре ?kind=... из фронтенда. */
public enum EventKind {
    INDICATOR("Показатель"),
    INCIDENT("Инцидент"),
    VET("Ветеринария"),
    WEIGHING("Взвешивание"),
    SLAUGHTER("Убой");

    private final String label;

    EventKind(String label) { this.label = label; }

    public String getLabel() { return label; }
}