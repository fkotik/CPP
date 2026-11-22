package ru.cpp.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.cpp.backend.entity.PoultryHouse;

// Это ИНТЕРФЕЙС без реализации. Spring Data сам создаст класс при старте.
// <PoultryHouse, Integer>: с какой сущностью работаем и какой тип у её @Id.
public interface PoultryHouseRepository extends JpaRepository<PoultryHouse, Integer> {

    // Spring разбирает ИМЯ метода и строит запрос:
    // findAll + OrderBy + Name + Asc
    //   → SELECT ... FROM poultry_house ORDER BY house_name ASC
    List<PoultryHouse> findAllByOrderByNameAsc();
}