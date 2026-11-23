package ru.cpp.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cpp.backend.dto.PageDto;
import ru.cpp.backend.dto.PassportEventDto;
import ru.cpp.backend.dto.PassportSummaryDto;
import ru.cpp.backend.exception.ApiException;
import ru.cpp.backend.repository.BatchRepository;
import ru.cpp.backend.repository.PassportRepository;

import java.util.List;

@Service
public class PassportService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PassportRepository passportRepository;
    private final BatchRepository batchRepository;

    public PassportService(PassportRepository passportRepository, BatchRepository batchRepository) {
        this.passportRepository = passportRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional(readOnly = true)
    public PageDto<PassportEventDto> events(Integer batchId, EventKind kind, int page, int size) {
        requireBatch(batchId);

        // Не доверяем входным числам: ?size=1000000 не должен положить сервер
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String kindCode = kind == null ? null : kind.name();

        List<PassportEventDto> items = passportRepository
                .findEvents(batchId, kindCode, safeSize, safePage * safeSize)
                .stream()
                .map(this::toDto)
                .toList();
        Integer total = passportRepository.countEvents(batchId, kindCode);

        return new PageDto<>(items, safePage, safeSize, total);
    }

    @Transactional(readOnly = true)
    public PassportSummaryDto summary(Integer batchId) {
        requireBatch(batchId);
        return new PassportSummaryDto(
                // Optional.orElse(null): "значение или null, если пусто"
                passportRepository.findSurvivalPercent(batchId).orElse(null),
                passportRepository.findLatestWeighing(batchId).orElse(null));
    }

    private PassportEventDto toDto(PassportRepository.RawEvent e) {
        EventKind kind = EventKind.valueOf(e.kind());   // строка "VET" -> константа EventKind.VET
        return new PassportEventDto(
                kind.name(),
                kind.getLabel(),
                e.id(),
                e.eventTime(),
                EventLabels.title(kind, e.title()),
                e.details(),
                e.source(),
                e.responsible(),
                e.alert());
    }

    private void requireBatch(Integer id) {
        if (!batchRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Партия не найдена");
        }
    }
}