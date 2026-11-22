package ru.cpp.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.cpp.backend.entity.AppUser;
import ru.cpp.backend.entity.Role;
import ru.cpp.backend.repository.AppUserRepository;
import ru.cpp.backend.repository.RoleRepository;

// Компонент создаётся ТОЛЬКО если в application.properties app.seed-demo-users=true.
// В продакшене просто выключаем свойство.
@Component
@ConditionalOnProperty(name = "app.seed-demo-users", havingValue = "true")
// CommandLineRunner: метод run() вызывается один раз после старта приложения
public class DemoDataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    // @Value читает значение из application.properties (аналог config('app.x') / env())
    public DemoDataSeeder(AppUserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.demo-password}") String demoPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seed("tech",   "Соколов",  "Игорь",  "TECHNOLOGIST", true);
        seed("vet",    "Орлова",   "Марина", "VET",          true);
        seed("otk",    "Громова",  "Елена",  "QC",           true);
        seed("boss",   "Нестеров", "Павел",  "MANAGER",      true);
        seed("admin",  "Лебедев",  "Артём",  "ADMIN",        true);
        // Деактивированный пользователь из макета
        seed("petrov", "Петров",   "Сергей", "TECHNOLOGIST", false);
    }

    private void seed(String login, String surname, String name, String roleCode, boolean active) {
        if (userRepository.existsByLogin(login)) {
            return; // уже есть, повторно не создаём
        }
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Нет роли " + roleCode));

        String hash = passwordEncoder.encode(demoPassword);
        userRepository.save(new AppUser(surname, name, null, login, hash, role, active));
    }
}