package com.bookstore.user.service;

import com.bookstore.common.dto.UserDTO;

public interface UserService {

    UserDTO getUserById(Long id);

    UserDTO getUserByEmail(String email);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    boolean existsByEmail(String email);
}
