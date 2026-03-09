package com.bookstore.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication responses.
 * Contains JWT tokens and user information returned after successful
 * authentication.
 */
@Data
@Builder
@NoArgsConstructor
public class AuthResponseDto {

    /**
     * JWT access token for authenticating API requests.
     * Short-lived token used in Authorization header.
     */
    private String token;

    /**
     * Refresh token for obtaining new access tokens.
     * Long-lived token used to refresh expired access tokens.
     */
    private String refreshToken;

    /**
     * User information for the authenticated user.
     * Contains profile details without sensitive data.
     */
    @lombok.Setter(lombok.AccessLevel.NONE)
    @lombok.Getter(lombok.AccessLevel.NONE)
    private UserDto user;

    public AuthResponseDto(String token, String refreshToken, UserDto user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user != null ? copyUser(user) : null;
    }

    public void setUser(UserDto user) {
        this.user = user != null ? copyUser(user) : null;
    }

    public UserDto getUser() {
        return user != null ? copyUser(user) : null;
    }

    private UserDto copyUser(UserDto original) {
        return UserDto.builder()
                .id(original.getId())
                .email(original.getEmail())
                .firstName(original.getFirstName())
                .lastName(original.getLastName())
                .role(original.getRole())
                .build();
    }
}
