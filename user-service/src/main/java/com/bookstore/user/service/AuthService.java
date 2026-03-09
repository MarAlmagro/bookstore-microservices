package com.bookstore.user.service;

import com.bookstore.common.dto.AuthRequestDto;
import com.bookstore.common.dto.AuthResponseDto;
import com.bookstore.common.dto.UserDto;

public interface AuthService {

    AuthResponseDto register(UserDto userDto, String password);

    AuthResponseDto login(AuthRequestDto authRequest);

    AuthResponseDto refreshToken(String refreshToken);
}
