package com.auth.dto;

import com.auth.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @Email
    private String email;

    @NotBlank
    private String password;

    private String name;

    private Role role; // ADMIN / ANALYST / VIEWER
}