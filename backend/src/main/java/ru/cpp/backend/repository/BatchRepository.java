package ru.cpp.backend.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.cpp.backend.entity.Batch;

import java.util.List;

// JpaSpecificationExecutor добавляет findAll(Specification, Sort): поиск с динамическими условиями
public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {

    boolean existsByBatchNumber(String batchNumber);

    // Свой запрос на JPQL (язык запросов по сущностям, а не по таблицам): все номера с префиксом
    @Query("select b.batchNumber from Batch b where b.batchNumber like concat(:prefix, '%')")
    List<String> findNumbersByPrefix(@Param("prefix") String prefix);

    // Переопределяем метод и навешиваем @EntityGraph: птичник подгрузится тем же запросом (JOIN).
    // Это аналог ->with('house') в Eloquent и лекарство от N+1.
    @Override
    @EntityGraph(attributePaths = "house")
    List<Batch> findAll(Specification<Batch> spec, Sort sort);
}