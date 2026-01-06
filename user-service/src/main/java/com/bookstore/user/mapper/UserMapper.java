package com.bookstore.user.mapper;

import com.bookstore.common.dto.UserDto;
import com.bookstore.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(dto.getRole() != null ? com.bookstore.common.constants.UserRole.valueOf(dto.getRole()) : null)
                .build();
    }
}
