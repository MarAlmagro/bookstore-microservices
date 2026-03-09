package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object for User information.
 * Used for transferring user profile data (without sensitive information like
 * password).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Unique identifier for the user.
     */
    private Long id;

    /**
     * Email address of the user.
     * Must be unique and valid email format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * First name of the user.
     */
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * Last name of the user.
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /**
     * Role of the user in the system.
     * Valid values: CUSTOMER, ADMIN
     */
    @NotBlank(message = "Role is required")
    private String role;
}
