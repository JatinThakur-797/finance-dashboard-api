package com.auth.service;

import com.auth.dto.CreateUserRequest;
import com.auth.dto.UpdateUserRequest;
import com.auth.dto.UserResponse;
import com.auth.entities.User;
import com.auth.exeptions.AuthException;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    //Create User
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("User already exists with this email");
        }
        if (request.getRole() == null) {
            throw new AuthException("Role is required");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        return new UserResponse(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::new)
                .toList();
    }

    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getActive() != null) user.setActive(request.getActive());
        user.setUpdatedAt(OffsetDateTime.now()); // ← you were missing this
        return new UserResponse(userRepository.save(user));
    }

    // Toggle status
    public UserResponse toggleUserStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthException("User not found"));

        user.setActive(!user.isActive());
        return new UserResponse(userRepository.save(user));
    }

    // Delete User (optional)
    public void deleteUser(UUID id, UUID currentUserId) {
        if(currentUserId.equals(id)){
            throw new AuthException("Admin Cannot delete himself");
        }
        userRepository.deleteById(id);
    }
}
