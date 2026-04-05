package com.auth.dto;

import com.auth.entities.User;
import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String name;
    private String role;
    private boolean active;

    public UserResponse(User u) {
        this.id = u.getId();
        this.email = u.getEmail();
        this.name = u.getName();
        this.role = u.getRole().name();
        this.active = u.isActive();
    }
}
