package com.bookstore.user.service;

import com.bookstore.common.constants.UserRole;
import com.bookstore.common.dto.AuthRequestDTO;
import com.bookstore.common.dto.AuthResponseDTO;
import com.bookstore.common.dto.UserDTO;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.UnauthorizedException;
import com.bookstore.user.entity.User;
import com.bookstore.user.mapper.UserMapper;
import com.bookstore.user.repository.UserRepository;
import com.bookstore.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserDTO testUserDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("CUSTOMER")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(testUserDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateTokenFromUsername(testUser.getEmail())).thenReturn("accessToken");
        when(tokenProvider.generateRefreshToken(testUser.getEmail())).thenReturn("refreshToken");
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        AuthResponseDTO result = authService.register(testUserDTO, "password123");

        assertNotNull(result);
        assertEquals("accessToken", result.getToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertNotNull(result.getUser());
        verify(userRepository).existsByEmail(testUserDTO.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        when(userRepository.existsByEmail(testUserDTO.getEmail())).thenReturn(true);

        assertThrows(InvalidRequestException.class, 
                () -> authService.register(testUserDTO, "password123"));
        verify(userRepository).existsByEmail(testUserDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        AuthRequestDTO authRequest = AuthRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("accessToken");
        when(tokenProvider.generateRefreshToken(authRequest.getEmail())).thenReturn("refreshToken");
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        AuthResponseDTO result = authService.login(authRequest);

        assertNotNull(result);
        assertEquals("accessToken", result.getToken());
        assertEquals("refreshToken", result.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials() {
        AuthRequestDTO authRequest = AuthRequestDTO.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        assertThrows(UnauthorizedException.class, () -> authService.login(authRequest));
    }

    @Test
    void refreshToken_Success() {
        String refreshToken = "validRefreshToken";
        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateTokenFromUsername("test@example.com")).thenReturn("newAccessToken");
        when(tokenProvider.generateRefreshToken("test@example.com")).thenReturn("newRefreshToken");
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        AuthResponseDTO result = authService.refreshToken(refreshToken);

        assertNotNull(result);
        assertEquals("newAccessToken", result.getToken());
        assertEquals("newRefreshToken", result.getRefreshToken());
        verify(tokenProvider).validateToken(refreshToken);
    }

    @Test
    void refreshToken_InvalidToken() {
        String refreshToken = "invalidRefreshToken";
        when(tokenProvider.validateToken(refreshToken)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(refreshToken));
    }
}
