package com.bookstore.user.controller;

import com.bookstore.common.dto.AuthRequestDto;
import com.bookstore.common.dto.AuthResponseDto;
import com.bookstore.common.dto.UserDto;
import com.bookstore.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        private AuthResponseDto authResponse;
        private UserDto userDto;

        @BeforeEach
        void setUp() {
                userDto = UserDto.builder()
                                .id(1L)
                                .email("test@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role("CUSTOMER")
                                .build();

                authResponse = AuthResponseDto.builder()
                                .token("accessToken")
                                .refreshToken("refreshToken")
                                .user(userDto)
                                .build();
        }

        @Test
        void register_Success() throws Exception {
                Map<String, Object> registerRequest = new HashMap<>();
                registerRequest.put("email", "test@example.com");
                registerRequest.put("password", "password123");
                registerRequest.put("firstName", "John");
                registerRequest.put("lastName", "Doe");

                when(authService.register(any(UserDto.class), anyString())).thenReturn(authResponse);

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").value("accessToken"))
                                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                                .andExpect(jsonPath("$.user.email").value("test@example.com"));
        }

        @Test
        void login_Success() throws Exception {
                AuthRequestDto loginRequest = AuthRequestDto.builder()
                                .email("test@example.com")
                                .password("password123")
                                .build();

                when(authService.login(any(AuthRequestDto.class))).thenReturn(authResponse);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("accessToken"))
                                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                                .andExpect(jsonPath("$.user.email").value("test@example.com"));
        }

        @Test
        void refreshToken_Success() throws Exception {
                Map<String, String> refreshRequest = new HashMap<>();
                refreshRequest.put("refreshToken", "validRefreshToken");

                when(authService.refreshToken(anyString())).thenReturn(authResponse);

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("accessToken"))
                                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
        }
}
