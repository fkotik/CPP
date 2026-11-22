package ru.cpp.backend.dto;

import ru.cpp.backend.entity.PoultryHouse;

public record HouseDto(Integer id, String name, Integer capacity, String characteristics) {

    // Фабричный метод: превращает сущность в DTO.
    // static: вызывается без создания объекта, как HouseDto.from(house)
    public static HouseDto from(PoultryHouse house) {
        return new HouseDto(
                house.getId(),
                house.getName(),
                house.getCapacity(),
                house.getCharacteristics());
    }
}