package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication responses.
 * Contains JWT tokens and user information returned after successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

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
    private UserDTO user;
}
