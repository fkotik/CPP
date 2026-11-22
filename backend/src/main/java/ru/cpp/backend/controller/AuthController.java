package ru.cpp.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ru.cpp.backend.dto.LoginRequest;
import ru.cpp.backend.dto.UserInfoDto;
import ru.cpp.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.authService = authService;
    }

    // POST /api/auth/login   тело: {"login":"tech","password":"Demo123!"}
    // @Valid: перед вызовом метода проверить аннотации в LoginRequest (@NotBlank).
    // При ошибке Spring сам вернёт 400.
    @PostMapping("/login")
    public UserInfoDto login(@Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication;
        try {
            // Проверка логина и пароля. При неудаче бросается AuthenticationException
            authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(body.login(), body.password()));
        } catch (AuthenticationException e) {
            // Одно и то же сообщение для всех причин (нет такого логина, неверный пароль,
            // учётка деактивирована): так не подсказываем злоумышленнику, что именно не так.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        // Защита от "фиксации сессии": если сессия уже была, сменить её идентификатор
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        // Запоминаем вошедшего пользователя в сессии
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return authService.getUserInfo(authentication.getName());
    }

    // GET /api/auth/me: кто я? React вызовет это при загрузке страницы,
    // чтобы понять, вошёл ли пользователь и какое меню показывать.
    // Параметр Authentication Spring подставляет сам (текущий пользователь).
    @GetMapping("/me")
    public UserInfoDto me(Authentication authentication) {
        return authService.getUserInfo(authentication.getName());
    }
}