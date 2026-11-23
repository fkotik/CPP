package ru.cpp.backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.cpp.backend.entity.ControlWeighing;

import java.util.List;

public interface ControlWeighingRepository extends JpaRepository<ControlWeighing, Integer> {

    // Имя метода - это и есть запрос (Spring Data разбирает его по словам):
    //   findBy Batch_Id  -> WHERE batch.id = ?   (подчёркивание = "зайти внутрь поля batch")
    //   OrderBy WeighingDate Asc, Id Asc -> ORDER BY weighing_date, weighing_id
    // @EntityGraph сразу подтягивает ответственного (JOIN), чтобы не было N+1.
    @EntityGraph(attributePaths = "responsible")
    List<ControlWeighing> findByBatch_IdOrderByWeighingDateAscIdAsc(Integer batchId);
}