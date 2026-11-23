package ru.cpp.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "control_weighing")
public class ControlWeighing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weighing_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(name = "weighing_date", nullable = false)
    private LocalDate weighingDate;

    @Column(name = "age_days", nullable = false)
    private int ageDays;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize;

    // все веса - граммы
    @Column(name = "actual_avg_weight", nullable = false, precision = 8, scale = 2)
    private BigDecimal actualAvgWeight;

    @Column(name = "planned_avg_weight", nullable = false, precision = 8, scale = 2)
    private BigDecimal plannedAvgWeight;

    @Column(name = "deviation_absolute", nullable = false, precision = 8, scale = 2)
    private BigDecimal deviationAbsolute;

    @Column(name = "deviation_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal deviationPercent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsible_user_id", nullable = false)
    private AppUser responsible;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Integer getId() { return id; }

    public Batch getBatch() { return batch; }
    public void setBatch(Batch batch) { this.batch = batch; }

    public LocalDate getWeighingDate() { return weighingDate; }
    public void setWeighingDate(LocalDate weighingDate) { this.weighingDate = weighingDate; }

    public int getAgeDays() { return ageDays; }
    public void setAgeDays(int ageDays) { this.ageDays = ageDays; }

    public int getSampleSize() { return sampleSize; }
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public BigDecimal getActualAvgWeight() { return actualAvgWeight; }
    public void setActualAvgWeight(BigDecimal v) { this.actualAvgWeight = v; }

    public BigDecimal getPlannedAvgWeight() { return plannedAvgWeight; }
    public void setPlannedAvgWeight(BigDecimal v) { this.plannedAvgWeight = v; }

    public BigDecimal getDeviationAbsolute() { return deviationAbsolute; }
    public void setDeviationAbsolute(BigDecimal v) { this.deviationAbsolute = v; }

    public BigDecimal getDeviationPercent() { return deviationPercent; }
    public void setDeviationPercent(BigDecimal v) { this.deviationPercent = v; }

    public AppUser getResponsible() { return responsible; }
    public void setResponsible(AppUser responsible) { this.responsible = responsible; }

    public Instant getCreatedAt() { return createdAt; }
}