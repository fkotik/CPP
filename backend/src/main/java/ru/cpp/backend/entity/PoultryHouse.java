package ru.cpp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// @Entity: этот класс соответствует таблице БД.
// @Table: имя таблицы, если оно отличается от имени класса.
@Entity
@Table(name = "poultry_house")
public class PoultryHouse {

    // @Id: первичный ключ.
    // IDENTITY: значение генерирует сама БД (наш GENERATED ALWAYS AS IDENTITY).
    // @Column: имя колонки, если оно отличается от имени поля.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_id")
    private Integer id;

    @Column(name = "house_name", nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    // Имя поля совпадает с именем колонки, @Column не обязателен
    private String characteristics;

    // JPA требует конструктор без параметров (он нужен Hibernate,
    // чтобы создавать объекты при чтении строк из БД).
    protected PoultryHouse() {
    }

    public PoultryHouse(String name, Integer capacity, String characteristics) {
        this.name = name;
        this.capacity = capacity;
        this.characteristics = characteristics;
    }

    // Геттеры: в Java принято делать поля private и давать доступ через методы
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getCharacteristics() {
        return characteristics;
    }
}