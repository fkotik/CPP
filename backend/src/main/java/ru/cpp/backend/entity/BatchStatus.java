package ru.cpp.backend.entity;

import java.util.Optional;

/**
 * Жизненный цикл партии. Порядок констант = порядок переходов.
 * В БД пишется имя константы (PLANTING, GROWING ...), русская подпись - только для интерфейса.
 */
public enum BatchStatus {
    PLANTING("Посадка"),
    GROWING("Выращивание"),
    WEIGHING("Взвешивание"),
    SLAUGHTER("Убой"),
    CLOSED("Закрыта");

    private final String label;

    BatchStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Следующий статус в цепочке. Для последнего (CLOSED) следующего нет -
     * поэтому возвращаем Optional, а не null.
     * ordinal() - порядковый номер константы (0, 1, 2 ...), значит, порядок объявления выше важен.
     */
    public Optional<BatchStatus> next() {
        int nextIndex = ordinal() + 1;
        return nextIndex < values().length
                ? Optional.of(values()[nextIndex])
                : Optional.empty();
    }
}