package ru.cpp.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Наше "ожидаемое" бизнес-исключение: сервис бросает его, когда операцию выполнить нельзя
 * (птичник мал, номер занят...). RuntimeException = непроверяемое: не нужно писать throws
 * в каждом методе. Аналог abort(422, 'сообщение') в Laravel.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}