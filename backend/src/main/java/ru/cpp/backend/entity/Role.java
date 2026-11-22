package ru.cpp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer id;

    // Стабильный технический код: TECHNOLOGIST, VET, QC, MANAGER, ADMIN
    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "role_name", nullable = false, unique = true)
    private String name;

    private String description;

    protected Role() {
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}