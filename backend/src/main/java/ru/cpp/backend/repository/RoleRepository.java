package ru.cpp.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.cpp.backend.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByCode(String code);
}