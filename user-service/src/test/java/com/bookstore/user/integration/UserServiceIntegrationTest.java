package com.bookstore.user.integration;

import com.bookstore.common.dto.AuthRequestDto;
import com.bookstore.common.dto.AuthResponseDto;
import com.bookstore.common.dto.UserDto;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import com.bookstore.user.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void fullAuthenticationFlow_Success() {
        UserDto registerDto = UserDto.builder()
                .email("integration@test.com")
                .firstName("Integration")
                .lastName("Test")
                .role("CUSTOMER")
                .build();

        AuthResponseDto registerResponse = authService.register(registerDto, "password123");
        assertNotNull(registerResponse);
        assertNotNull(registerResponse.getToken());
        assertNotNull(registerResponse.getRefreshToken());
        assertEquals("integration@test.com", registerResponse.getUser().getEmail());

        User savedUser = userRepository.findByEmail("integration@test.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("Integration", savedUser.getFirstName());
        assertTrue(savedUser.getEnabled());

        AuthRequestDto loginRequest = AuthRequestDto.builder()
                .email("integration@test.com")
                .password("password123")
                .build();

        AuthResponseDto loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals("integration@test.com", loginResponse.getUser().getEmail());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        AuthResponseDto refreshResponse = authService.refreshToken(loginResponse.getRefreshToken());
        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getToken());
        assertNotEquals(loginResponse.getToken(), refreshResponse.getToken());
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        UserDto userDto = UserDto.builder()
                .email("duplicate@test.com")
                .firstName("First")
                .lastName("User")
                .build();

        authService.register(userDto, "password123");

        UserDto duplicateDto = UserDto.builder()
                .email("duplicate@test.com")
                .firstName("Second")
                .lastName("User")
                .build();

        assertThrows(Exception.class, () -> authService.register(duplicateDto, "password123"));
    }
}
