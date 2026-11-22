package ru.cpp.backend.controller;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ВРЕМЕННЫЙ контроллер для проверки ролей. Удалим после Шага 5.
@RestController
@RequestMapping("/api/test")
public class SecurityTestController {

    // Любой вошедший пользователь (правило anyRequest().authenticated())
    @GetMapping("/any")
    public Map<String, Object> any(Authentication auth) {
        return Map.of("user", auth.getName(), "authorities", auth.getAuthorities().toString());
    }

    // @PreAuthorize: проверка ПЕРЕД выполнением метода. Не подходит роль, то 403.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Map<String, String> admin() {
        return Map.of("access", "admin only");
    }

    @PreAuthorize("hasRole('TECHNOLOGIST')")
    @GetMapping("/technologist")
    public Map<String, String> technologist() {
        return Map.of("access", "technologist only");
    }

    // Несколько ролей
    @PreAuthorize("hasAnyRole('TECHNOLOGIST', 'MANAGER')")
    @GetMapping("/tech-or-manager")
    public Map<String, String> techOrManager() {
        return Map.of("access", "technologist or manager");
    }
}