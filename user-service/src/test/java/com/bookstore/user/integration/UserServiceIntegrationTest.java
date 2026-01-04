package com.bookstore.user.integration;

import com.bookstore.common.constants.UserRole;
import com.bookstore.common.dto.AuthRequestDTO;
import com.bookstore.common.dto.AuthResponseDTO;
import com.bookstore.common.dto.UserDTO;
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
        UserDTO registerDTO = UserDTO.builder()
                .email("integration@test.com")
                .firstName("Integration")
                .lastName("Test")
                .role("CUSTOMER")
                .build();

        AuthResponseDTO registerResponse = authService.register(registerDTO, "password123");
        assertNotNull(registerResponse);
        assertNotNull(registerResponse.getToken());
        assertNotNull(registerResponse.getRefreshToken());
        assertEquals("integration@test.com", registerResponse.getUser().getEmail());

        User savedUser = userRepository.findByEmail("integration@test.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("Integration", savedUser.getFirstName());
        assertTrue(savedUser.getEnabled());

        AuthRequestDTO loginRequest = AuthRequestDTO.builder()
                .email("integration@test.com")
                .password("password123")
                .build();

        AuthResponseDTO loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals("integration@test.com", loginResponse.getUser().getEmail());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        AuthResponseDTO refreshResponse = authService.refreshToken(loginResponse.getRefreshToken());
        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getToken());
        assertNotEquals(loginResponse.getToken(), refreshResponse.getToken());
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        UserDTO userDTO = UserDTO.builder()
                .email("duplicate@test.com")
                .firstName("First")
                .lastName("User")
                .build();

        authService.register(userDTO, "password123");

        UserDTO duplicateDTO = UserDTO.builder()
                .email("duplicate@test.com")
                .firstName("Second")
                .lastName("User")
                .build();

        assertThrows(Exception.class, () -> authService.register(duplicateDTO, "password123"));
    }
}
