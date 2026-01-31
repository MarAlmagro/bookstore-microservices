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

class UserServiceIntegrationTest extends BasePostgresIntegrationTest {

    private static final String TEST_EMAIL = "integration@test.com";
    private static final String TEST_PASSWORD = "password123";

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
                .email(TEST_EMAIL)
                .firstName("Integration")
                .lastName("Test")
                .role("CUSTOMER")
                .build();

        AuthResponseDto registerResponse = authService.register(registerDto, TEST_PASSWORD);
        assertNotNull(registerResponse);
        assertNotNull(registerResponse.getToken());
        assertNotNull(registerResponse.getRefreshToken());
        assertEquals(TEST_EMAIL, registerResponse.getUser().getEmail());

        User savedUser = userRepository.findByEmail(TEST_EMAIL).orElse(null);
        assertNotNull(savedUser);
        assertEquals("Integration", savedUser.getFirstName());
        assertTrue(savedUser.getEnabled());

        AuthRequestDto loginRequest = AuthRequestDto.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        AuthResponseDto loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals(TEST_EMAIL, loginResponse.getUser().getEmail());

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

        authService.register(userDto, TEST_PASSWORD);

        UserDto duplicateDto = UserDto.builder()
                .email("duplicate@test.com")
                .firstName("Second")
                .lastName("User")
                .build();

        assertThrows(Exception.class, () -> authService.register(duplicateDto, TEST_PASSWORD));
    }
}
