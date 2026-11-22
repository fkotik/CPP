package ru.cpp.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Тело любого ответа с ошибкой. fieldErrors заполняется только для ошибок формы. */
@JsonInclude(JsonInclude.Include.NON_NULL) // null-поля в JSON не выводим
public record ApiError(String message, Map<String, String> fieldErrors) {
}