package com.auth.dto;

import com.auth.entities.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private Role role;
    private Boolean active;
}