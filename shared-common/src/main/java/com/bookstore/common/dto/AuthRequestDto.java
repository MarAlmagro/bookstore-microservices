package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object for authentication/login requests.
 * Contains user credentials for authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

    /**
     * Email address of the user attempting to authenticate.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Password for authentication.
     * Minimum length of 6 characters for security.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
