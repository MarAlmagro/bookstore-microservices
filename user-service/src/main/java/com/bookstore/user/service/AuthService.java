package com.bookstore.user.service;

import com.bookstore.common.dto.AuthRequestDTO;
import com.bookstore.common.dto.AuthResponseDTO;
import com.bookstore.common.dto.UserDTO;

public interface AuthService {

    AuthResponseDTO register(UserDTO userDTO, String password);

    AuthResponseDTO login(AuthRequestDTO authRequest);

    AuthResponseDTO refreshToken(String refreshToken);
}
