package ru.cpp.backend.service;

import java.util.List;

/** Допустимые кроссы. Должен совпадать с CHECK в миграции V2 и с growth_norm. */
public final class CrossBreeds {

    public static final List<String> ALL = List.of("Ross 308", "Cobb 500", "Hubbard F15");

    private CrossBreeds() {
    } // запрещаем создавать экземпляры: это просто "полка с константами"
}