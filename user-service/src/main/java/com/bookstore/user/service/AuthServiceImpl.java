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
import com.bookstore.user.security.LoginAttemptService;
import com.bookstore.common.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public AuthResponseDto register(UserDto userDto, String password) {
        logger.debug("Registering new user with email: {}", userDto.getEmail());

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new InvalidRequestException("Email already exists: " + userDto.getEmail());
        }

        User user = User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(password))
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .role(userDto.getRole() != null ? UserRole.valueOf(userDto.getRole()) : UserRole.CUSTOMER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with email: {}", userDto.getEmail());

        String token = tokenProvider.generateTokenWithClaims(savedUser.getEmail(), savedUser.getId(), savedUser.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(savedUser))
                .build();
    }

    @Override
    public AuthResponseDto login(AuthRequestDto authRequest) {
        logger.debug("User login attempt with email: {}", authRequest.getEmail());

        String loginKey = authRequest.getEmail();

        if (loginAttemptService.isBlocked(loginKey)) {
            logger.warn("Login blocked for email: {} due to too many failed attempts", authRequest.getEmail());
            throw new UnauthorizedException("Account temporarily locked due to too many failed login attempts. Please try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            loginAttemptService.loginSucceeded(loginKey);

            String token = tokenProvider.generateTokenWithClaims(user.getEmail(), user.getId(), user.getRole().name());
            String refreshToken = tokenProvider.generateRefreshToken(authRequest.getEmail());

            logger.info("User logged in successfully with email: {}", authRequest.getEmail());

            return AuthResponseDto.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .user(userMapper.toDto(user))
                    .build();

        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(loginKey);
            logger.error("Authentication failed for email: {}", authRequest.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        logger.debug("Refreshing token");

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String newToken = tokenProvider.generateTokenWithClaims(user.getEmail(), user.getId(), user.getRole().name());
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        logger.info("Token refreshed successfully for email: {}", email);

        return AuthResponseDto.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toDto(user))
                .build();
    }
}
