package com.cinetime.payload.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequestDirect {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(
            regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
            message="Password must contain upper, lower, digit and special char, min 8"
    )
    private String newPassword;
}

