package ru.cpp.backend.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.cpp.backend.dto.WeighingCalcDto;
import ru.cpp.backend.dto.WeighingCreateRequest;
import ru.cpp.backend.dto.WeighingDto;
import ru.cpp.backend.service.WeighingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/batches/{batchId}/weighings")
public class WeighingController {

    private final WeighingService weighingService;

    public WeighingController(WeighingService weighingService) {
        this.weighingService = weighingService;
    }

    // Список по возрастанию даты (удобно для графика)
    @GetMapping
    @PreAuthorize(BatchController.VIEW)
    public List<WeighingDto> list(@PathVariable Integer batchId) {
        return weighingService.list(batchId);
    }

    // GET /api/batches/5/weighings/preview?weighingDate=2025-03-29&actualAvgWeight=1450
    @GetMapping("/preview")
    @PreAuthorize("hasRole('TECHNOLOGIST')")
    public WeighingCalcDto preview(
            @PathVariable Integer batchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weighingDate,
            @RequestParam BigDecimal actualAvgWeight) {
        return weighingService.preview(batchId, weighingDate, actualAvgWeight);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TECHNOLOGIST')")
    public WeighingDto create(@PathVariable Integer batchId,
                              @Valid @RequestBody WeighingCreateRequest request,
                              Authentication auth) {
        return weighingService.create(batchId, request, auth.getName());
    }
}