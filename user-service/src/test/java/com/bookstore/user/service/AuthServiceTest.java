package com.bookstore.user.service;

import com.bookstore.common.constants.UserRole;
import com.bookstore.common.dto.AuthRequestDto;
import com.bookstore.common.dto.AuthResponseDto;
import com.bookstore.common.dto.UserDto;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.UnauthorizedException;
import com.bookstore.user.entity.User;
import com.bookstore.user.mapper.UserMapper;
import com.bookstore.user.repository.UserRepository;
import com.bookstore.common.security.JwtTokenProvider;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = REFRESH_TOKEN;

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

    private UserDto testUserDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .email(TEST_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .role("CUSTOMER")
                .build();

        testUser = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(testUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateTokenWithClaims(eq(testUser.getEmail()), eq(testUser.getId()), eq(testUser.getRole().name()))).thenReturn(ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(testUser.getEmail())).thenReturn(REFRESH_TOKEN);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        AuthResponseDto result = authService.register(testUserDto, TEST_PASSWORD);

        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result.getToken());
        assertEquals(REFRESH_TOKEN, result.getRefreshToken());
        assertNotNull(result.getUser());
        verify(userRepository).existsByEmail(testUserDto.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        when(userRepository.existsByEmail(testUserDto.getEmail())).thenReturn(true);

        assertThrows(InvalidRequestException.class, 
                () -> authService.register(testUserDto, TEST_PASSWORD));
        verify(userRepository).existsByEmail(testUserDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        AuthRequestDto authRequest = AuthRequestDto.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateTokenWithClaims(eq(testUser.getEmail()), eq(testUser.getId()), eq(testUser.getRole().name()))).thenReturn(ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(authRequest.getEmail())).thenReturn(REFRESH_TOKEN);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        AuthResponseDto result = authService.login(authRequest);

        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result.getToken());
        assertEquals(REFRESH_TOKEN, result.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials() {
        AuthRequestDto authRequest = AuthRequestDto.builder()
                .email(TEST_EMAIL)
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
        when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateTokenWithClaims(eq(testUser.getEmail()), eq(testUser.getId()), eq(testUser.getRole().name()))).thenReturn("newAccessToken");
        when(tokenProvider.generateRefreshToken(TEST_EMAIL)).thenReturn("newRefreshToken");
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        AuthResponseDto result = authService.refreshToken(refreshToken);

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
