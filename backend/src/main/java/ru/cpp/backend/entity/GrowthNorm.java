package ru.cpp.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Одна точка нормативной кривой роста: кросс + возраст -> средний вес в граммах. */
@Entity
@Table(name = "growth_norm")
public class GrowthNorm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "growth_norm_id")
    private Integer id;

    @Column(name = "cross_breed", nullable = false, length = 50)
    private String crossBreed;

    @Column(name = "age_days", nullable = false)
    private int ageDays;

    @Column(name = "avg_weight_g", nullable = false, precision = 8, scale = 2)
    private BigDecimal avgWeightG;

    public Integer getId() { return id; }
    public String getCrossBreed() { return crossBreed; }
    public int getAgeDays() { return ageDays; }
    public BigDecimal getAvgWeightG() { return avgWeightG; }
}