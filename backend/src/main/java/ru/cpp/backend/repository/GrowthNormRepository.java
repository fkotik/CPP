package ru.cpp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cpp.backend.entity.GrowthNorm;

import java.util.List;

public interface GrowthNormRepository extends JpaRepository<GrowthNorm, Integer> {

    List<GrowthNorm> findByCrossBreedOrderByAgeDaysAsc(String crossBreed);
}