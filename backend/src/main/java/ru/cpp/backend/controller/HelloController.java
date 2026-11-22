package ru.cpp.backend.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RestController: "этот класс принимает HTTP-запросы,
// а то, что возвращают его методы, нужно превращать в JSON".
@RestController
// @RequestMapping: общий префикс URL для всех методов класса.
// (аналог Route::prefix('api') в Laravel)
@RequestMapping("/api")
public class HelloController {

    // @GetMapping: обрабатывает GET-запрос по адресу /api/hello
    // (аналог Route::get('/api/hello', ...))
    @GetMapping("/hello")
    public HelloResponse hello(
            // @RequestParam: берёт параметр из адреса ?name=...
            // (аналог $request->query('name', 'мир') в Laravel)
            @RequestParam(defaultValue = "мир") String name) {

        return new HelloResponse(
                "Цифровой паспорт партии",
                "Привет, " + name + "!",
                LocalDateTime.now());
    }

    // record - краткая запись неизменяемого класса-"контейнера данных" (Java 16+).
    // Spring превратит его в JSON: {"app": "...", "message": "...", "time": "..."}
    // Это аналог того, как Laravel превращает массив/Resource в JSON.
    public record HelloResponse(String app, String message, LocalDateTime time) {
    }
}