package ru.cpp.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cpp.backend.dto.PageDto;
import ru.cpp.backend.dto.PassportEventDto;
import ru.cpp.backend.dto.PassportSummaryDto;
import ru.cpp.backend.service.EventKind;
import ru.cpp.backend.service.PassportService;

@RestController
@RequestMapping("/api/batches/{batchId}")
public class PassportController {

    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    // GET /api/batches/5/events?kind=VET&page=0&size=20
    // Без kind - все события ("Все" в макете). Права те же, что на просмотр партии.
    @GetMapping("/events")
    @PreAuthorize(BatchController.VIEW)
    public PageDto<PassportEventDto> events(
            @PathVariable Integer batchId,
            @RequestParam(required = false) EventKind kind,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return passportService.events(batchId, kind, page, size);
    }

    // Сохранность и данные для красного баннера отклонения
    @GetMapping("/summary")
    @PreAuthorize(BatchController.VIEW)
    public PassportSummaryDto summary(@PathVariable Integer batchId) {
        return passportService.summary(batchId);
    }
}