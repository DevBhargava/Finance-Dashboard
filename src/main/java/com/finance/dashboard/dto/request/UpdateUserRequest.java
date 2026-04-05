package com.finance.dashboard.dto.request;

import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 100)
    private String name;

    private Role role;

    private UserStatus status;

    // optional password reset — null means don't change
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
