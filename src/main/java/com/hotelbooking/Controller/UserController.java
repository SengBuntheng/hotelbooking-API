package com.hotelbooking.Controller;

import com.hotelbooking.dto.ApiResponse;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserResponse;
import com.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            List<UserResponse> users = userService.findAll();
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            log.error("Failed to retrieve users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve users"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.findbyid(id);
            return ResponseEntity.ok(ApiResponse.success("User found", user));
        } catch (IllegalArgumentException e) {
            log.warn("User not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with id: " + id));
        } catch (Exception e) {
            log.error("Failed to retrieve user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponse user = userService.findByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
        } catch (Exception e) {
            log.error("Failed to retrieve profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve profile"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        try {
            UserResponse updatedUser = userService.Update(id, userRequest);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found or update failed"));
        } catch (Exception e) {
            log.error("Failed to update user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update user"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UserRequest userRequest,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponse currentUser = userService.findByEmail(email);
            UserResponse updatedUser = userService.Update(currentUser.getId(), userRequest);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Failed to update profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update profile"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete user"));
        }
    }
}
