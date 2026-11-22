package ru.cpp.backend.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.cpp.backend.entity.AppUser;
import ru.cpp.backend.repository.AppUserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // findByLogin возвращает Optional<AppUser>.
        // orElseThrow: "достань значение, а если пусто, брось исключение".
        AppUser user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        // Это класс Spring Security (не наша сущность): "карточка" для проверки входа.
        return User.withUsername(user.getLogin())
                .password(user.getPasswordHash())
                // Spring Security различает "роль" и "право" по префиксу ROLE_.
                // hasRole('ADMIN') проверяет наличие именно "ROLE_ADMIN".
                .authorities("ROLE_" + user.getRole().getCode())
                // Деактивированный пользователь войти не сможет
                .disabled(!user.isActive())
                .build();
    }
}