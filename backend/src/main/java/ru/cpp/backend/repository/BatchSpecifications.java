package ru.cpp.backend.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.cpp.backend.entity.Batch;
import ru.cpp.backend.entity.BatchStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Сборка WHERE "по кусочкам". Аналог цепочки в Laravel:
 *   Batch::query()->when($search, fn($q) => $q->where(...))->when($status, ...)
 * Каждое условие добавляется, только если фильтр передан.
 */
public final class BatchSpecifications {

    private BatchSpecifications() {
    }

    public static Specification<Batch> filter(String search, Integer houseId, BatchStatus status,
                                              LocalDate plantedFrom, LocalDate plantedTo) {
        // Specification - функция (root, query, cb) -> Predicate.
        // root = таблица batch, cb = "строитель" условий
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim() + "%";
                // lower() с обеих сторон делает именно БД, поэтому регистр сравнивается одинаково
                conditions.add(cb.like(
                        cb.lower(root.<String>get("batchNumber")),
                        cb.lower(cb.literal(pattern))));
            }
            if (houseId != null) {
                conditions.add(cb.equal(root.get("house").get("id"), houseId));
            }
            if (status != null) {
                conditions.add(cb.equal(root.get("status"), status));
            }
            if (plantedFrom != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("plantingDate"), plantedFrom));
            }
            if (plantedTo != null) {
                conditions.add(cb.lessThanOrEqualTo(root.<LocalDate>get("plantingDate"), plantedTo));
            }
            // and() от пустого массива = "условий нет" = все строки
            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}