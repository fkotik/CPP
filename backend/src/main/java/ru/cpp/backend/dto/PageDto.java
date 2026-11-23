package ru.cpp.backend.dto;

import java.util.List;

/**
 * Универсальная "страница результатов". <T> - обобщённый тип (generics): PageDto<PassportEventDto>
 * это страница событий, PageDto<BatchListItemDto> была бы страницей партий. Код один, типы разные.
 * В PHP это только комментарий /** @template T *​/, а в Java компилятор реально проверяет типы.
 */
public record PageDto<T>(List<T> items, int page, int size, long total) {
}