package ru.cpp.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.cpp.backend.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    // Optional: "пользователь есть или его нет" (вместо null)
    Optional<AppUser> findByLogin(String login);

    boolean existsByLogin(String login);
}