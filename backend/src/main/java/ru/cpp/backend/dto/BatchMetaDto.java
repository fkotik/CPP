package ru.cpp.backend.dto;

import ru.cpp.backend.entity.BatchStatus;
import ru.cpp.backend.service.CrossBreeds;

import java.util.Arrays;
import java.util.List;

public record BatchMetaDto(List<String> crossBreeds, List<StatusOption> statuses) {

    // Вложенный record: пара "код - подпись"
    public record StatusOption(String code, String label) {
    }

    public static BatchMetaDto build() {
        List<StatusOption> statuses = Arrays.stream(BatchStatus.values())
                .map(s -> new StatusOption(s.name(), s.getLabel()))
                .toList();
        return new BatchMetaDto(CrossBreeds.ALL, statuses);
    }
}