package ru.cpp.backend.dto;

import ru.cpp.backend.entity.AppUser;

public record UserInfoDto(Integer id, String login, String fullName, String roleCode, String roleName) {

    public static UserInfoDto from(AppUser user) {
        return new UserInfoDto(
                user.getId(),
                user.getLogin(),
                user.getFullName(),
                user.getRole().getCode(),
                user.getRole().getName());
    }
}