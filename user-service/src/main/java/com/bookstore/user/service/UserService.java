package com.bookstore.user.service;

import com.bookstore.common.dto.UserDto;

public interface UserService {

    UserDto getUserById(Long id);

    UserDto getUserByEmail(String email);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    boolean existsByEmail(String email);
}
