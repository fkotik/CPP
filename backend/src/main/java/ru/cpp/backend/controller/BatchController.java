package ru.cpp.backend.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.cpp.backend.dto.BatchCreateRequest;
import ru.cpp.backend.dto.BatchDetailDto;
import ru.cpp.backend.dto.BatchListItemDto;
import ru.cpp.backend.dto.BatchMetaDto;
import ru.cpp.backend.entity.BatchStatus;
import ru.cpp.backend.service.BatchService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    // Кто может смотреть партии. Администратор НЕ в списке: он только управляет пользователями.
    public static final String VIEW = "hasAnyRole('TECHNOLOGIST','VET','QC','MANAGER')";

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    // Справочники для выпадающих списков (кроссы, статусы)
    @GetMapping("/meta")
    @PreAuthorize(VIEW)
    public BatchMetaDto meta() {
        return BatchMetaDto.build();
    }

    // GET /api/batches?search=015&houseId=1&status=GROWING&plantedFrom=2025-01-01&plantedTo=2025-12-31
    // Все параметры необязательные. status Spring сам превращает из строки в enum BatchStatus.
    @GetMapping
    @PreAuthorize(VIEW)
    public List<BatchListItemDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer houseId,
            @RequestParam(required = false) BatchStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plantedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plantedTo) {
        return batchService.list(search, houseId, status, plantedFrom, plantedTo);
    }

    @GetMapping("/{id}")
    @PreAuthorize(VIEW)
    public BatchDetailDto get(@PathVariable Integer id, Authentication auth) {
        // Authentication Spring подставляет сам: это "кто сейчас вошёл". getName() = логин.
        return batchService.get(id, auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)          // 201 вместо 200
    @PreAuthorize("hasRole('TECHNOLOGIST')")     // создавать может только технолог
    public BatchDetailDto create(@Valid @RequestBody BatchCreateRequest request, Authentication auth) {
        return batchService.create(request, auth.getName());
    }

    // Кнопка "Перевести в ...". Точное правило "кто из какого статуса" - внутри сервиса.
    @PostMapping("/{id}/advance")
    @PreAuthorize("hasAnyRole('TECHNOLOGIST','QC')")
    public BatchDetailDto advance(@PathVariable Integer id, Authentication auth) {
        return batchService.advance(id, auth.getName());
    }
}