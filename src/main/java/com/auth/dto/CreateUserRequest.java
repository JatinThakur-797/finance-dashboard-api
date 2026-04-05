package com.auth.dto;

import com.auth.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {

    @Email
    private String email;

    @NotBlank
    private String password;

    private String name;

    private Role role; // ADMIN / ANALYST / VIEWER
}