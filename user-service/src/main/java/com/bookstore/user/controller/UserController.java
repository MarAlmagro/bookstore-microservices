package com.bookstore.user.controller;

import com.bookstore.common.constants.ApiConstants;
import com.bookstore.common.dto.UserDto;
import com.bookstore.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/users")
@Tag(name = "Users", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Updates the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateCurrentUserProfile(@Valid @RequestBody UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserDto currentUser = userService.getUserByEmail(email);
        UserDto updatedUser = userService.updateUser(currentUser.getId(), userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns user profile by ID (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user account (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
