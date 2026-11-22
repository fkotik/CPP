package ru.cpp.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.cpp.backend.dto.HouseDto;
import ru.cpp.backend.service.PoultryHouseService;

@RestController
@RequestMapping("/api/houses")
public class HouseController {

    private final PoultryHouseService service;

    public HouseController(PoultryHouseService service) {
        this.service = service;
    }

    // GET /api/houses
    @GetMapping
    public List<HouseDto> list() {
        return service.findAll();
    }
}