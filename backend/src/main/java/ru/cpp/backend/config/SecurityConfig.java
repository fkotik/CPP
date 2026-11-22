package ru.cpp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

// @Configuration: класс с настройками, внутри методы @Bean.
// Каждый @Bean-метод создаёт объект, который Spring кладёт в контейнер
// (так же, как $this->app->singleton(...) в ServiceProvider Laravel).
@Configuration
// Включает аннотации @PreAuthorize на методах контроллеров и сервисов
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contextRepository) throws Exception {
        http
                // CSRF-защиту отключаем: API работает с React через JSON, а cookie у нас SameSite=Lax.
                // Для учебного MVP это допустимо. В боевой системе включили бы CSRF-токен.
                .csrf(csrf -> csrf.disable())

                // Где хранить "кто вошёл": в HTTP-сессии
                .securityContext(sc -> sc.securityContextRepository(contextRepository))

                // Правила доступа к URL. Проверяются СВЕРХУ ВНИЗ, срабатывает первое подходящее.
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/login").permitAll().requestMatchers("/api/hello").permitAll()
                        // /error нужен, чтобы клиент видел текст ошибки, а не пустой ответ
                        .requestMatchers("/error").permitAll()
                        // Всё остальное: только для вошедших пользователей.
                        // Тонкие правила по ролям ставим на методах через @PreAuthorize.
                        .anyRequest().authenticated())

                // Для REST API: не перенаправлять на страницу логина, а отвечать 401
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // POST /api/auth/logout: убить сессию и ответить 200
                .logout(logout -> logout.logoutUrl("/api/auth/logout").logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));

        // Страницу входа Spring (formLogin) и httpBasic мы не включаем: вход у нас свой, JSON.
        return http.build();
    }

    // Хэширование паролей: BCrypt (требование спецификации п. 5.5).
    // BCrypt каждый раз добавляет случайную "соль", поэтому два одинаковых пароля
    // дают разные хэши. Проверять можно только через matches().
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager: "проверяльщик" логина и пароля.
    // Он сам найдёт наш UserDetailsService и PasswordEncoder.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}