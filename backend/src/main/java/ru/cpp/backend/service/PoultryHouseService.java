package ru.cpp.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.cpp.backend.dto.HouseDto;
import ru.cpp.backend.repository.PoultryHouseRepository;

@Service
public class PoultryHouseService {

    private final PoultryHouseRepository repository;

    // Внедрение зависимости через конструктор (см. 4.2)
    public PoultryHouseService(PoultryHouseRepository repository) {
        this.repository = repository;
    }

    // @Transactional(readOnly = true): метод выполняется в транзакции БД
    // только для чтения. Для методов, которые изменяют данные,
    // пишут просто @Transactional.
    @Transactional(readOnly = true)
    public List<HouseDto> findAll() {
        return repository.findAllByOrderByNameAsc()
                .stream()
                .map(HouseDto::from)
                .toList();
    }
}