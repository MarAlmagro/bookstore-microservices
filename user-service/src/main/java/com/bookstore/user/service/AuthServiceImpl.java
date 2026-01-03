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

    @Override
    public AuthResponseDTO register(UserDTO userDTO, String password) {
        logger.debug("Registering new user with email: {}", userDTO.getEmail());

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new InvalidRequestException("Email already exists: " + userDTO.getEmail());
        }

        User user = User.builder()
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(password))
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .role(userDTO.getRole() != null ? userDTO.getRole() : UserRole.CUSTOMER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with email: {}", userDTO.getEmail());

        String token = tokenProvider.generateTokenFromUsername(savedUser.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userMapper.toDTO(savedUser))
                .build();
    }

    @Override
    public AuthResponseDTO login(AuthRequestDTO authRequest) {
        logger.debug("User login attempt with email: {}", authRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            String token = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authRequest.getEmail());

            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            logger.info("User logged in successfully with email: {}", authRequest.getEmail());

            return AuthResponseDTO.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .user(userMapper.toDTO(user))
                    .build();

        } catch (AuthenticationException e) {
            logger.error("Authentication failed for email: {}", authRequest.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        logger.debug("Refreshing token");

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String newToken = tokenProvider.generateTokenFromUsername(email);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        logger.info("Token refreshed successfully for email: {}", email);

        return AuthResponseDTO.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toDTO(user))
                .build();
    }
}
