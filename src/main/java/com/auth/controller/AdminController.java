package com.auth.controller;

import com.auth.dto.CreateUserRequest;
import com.auth.dto.UpdateUserRequest;
import com.auth.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // Create User
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    // Get All User
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // Update User
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id,
                                        @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    // Toggle Status
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }

    // Delete User
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id,
                                        Authentication authentication) {

        UUID currentUserId = UUID.fromString((String) authentication.getPrincipal());

        adminService.deleteUser(id, currentUserId);

        return ResponseEntity.ok("User deleted successfully");
    }
}
