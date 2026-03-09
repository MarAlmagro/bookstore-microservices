package com.bookstore.user.controller;

import com.bookstore.common.dto.UserDto;
import com.bookstore.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

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
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = { "CUSTOMER" })
    void getCurrentUserProfile_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getCurrentUserProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = { "CUSTOMER" })
    void updateCurrentUserProfile_Success() throws Exception {
        UserDto updateDto = UserDto.builder()
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role("CUSTOMER")
                .build();

        UserDto updatedDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role("CUSTOMER")
                .build();

        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = { "ADMIN" })
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = { "CUSTOMER" })
    void getUserById_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 403 && status != 500) {
                        throw new AssertionError("Expected 403 or 500 but was: " + status);
                    }
                });
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = { "ADMIN" })
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }
}
