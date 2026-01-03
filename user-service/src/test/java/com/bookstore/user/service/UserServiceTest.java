package com.bookstore.user.service;

import com.bookstore.common.constants.UserRole;
import com.bookstore.common.dto.UserDTO;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.user.entity.User;
import com.bookstore.user.mapper.UserMapper;
import com.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("CUSTOMER")
                .build();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).findById(1L);
        verify(userMapper).toDTO(testUser);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void updateUser_Success() {
        UserDTO updateDTO = UserDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Jane")
                .lastName("Smith")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(testUserDTO);

        UserDTO result = userService.updateUser(1L, updateDTO);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("test@example.com");

        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }
}
