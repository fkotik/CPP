package ru.cpp.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "batch")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long id;

    @Column(name = "batch_number", nullable = false, unique = true, length = 20)
    private String batchNumber;

    // LAZY: птичник подгружается из БД только когда к нему обратились (см. пояснение ниже)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_id", nullable = false)
    private PoultryHouse house;

    @Column(name = "planting_date", nullable = false)
    private LocalDate plantingDate;

    @Column(name = "initial_count", nullable = false)
    private int initialCount;

    @Column(name = "source_supplier", nullable = false, length = 200)
    private String sourceSupplier;

    @Column(name = "cross_breed", nullable = false, length = 50)
    private String crossBreed;

    // граммы
    @Column(name = "planned_weight", nullable = false, precision = 8, scale = 2)
    private BigDecimal plannedWeight;

    @Column(name = "planned_slaughter_date", nullable = false)
    private LocalDate plannedSlaughterDate;

    // STRING: в БД пишется имя константы ("PLANTING"), а не её номер
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status = BatchStatus.PLANTING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    /** Вызывается Hibernate прямо перед первым INSERT. Аналог события creating в Eloquent. */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // ---- getters / setters (JPA требует конструктор без аргументов - он есть неявно) ----

    public Long getId() { return id; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public PoultryHouse getHouse() { return house; }
    public void setHouse(PoultryHouse house) { this.house = house; }

    public LocalDate getPlantingDate() { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }

    public int getInitialCount() { return initialCount; }
    public void setInitialCount(int initialCount) { this.initialCount = initialCount; }

    public String getSourceSupplier() { return sourceSupplier; }
    public void setSourceSupplier(String sourceSupplier) { this.sourceSupplier = sourceSupplier; }

    public String getCrossBreed() { return crossBreed; }
    public void setCrossBreed(String crossBreed) { this.crossBreed = crossBreed; }

    public BigDecimal getPlannedWeight() { return plannedWeight; }
    public void setPlannedWeight(BigDecimal plannedWeight) { this.plannedWeight = plannedWeight; }

    public LocalDate getPlannedSlaughterDate() { return plannedSlaughterDate; }
    public void setPlannedSlaughterDate(LocalDate d) { this.plannedSlaughterDate = d; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public AppUser getCreatedBy() { return createdBy; }
    public void setCreatedBy(AppUser createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}