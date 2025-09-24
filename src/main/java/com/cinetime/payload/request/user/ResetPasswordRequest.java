package com.cinetime.payload.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64, message = "New password must be 8–64 characters")
    // at least 1 upper, 1 lower, 1 digit, 1 special (adjust if you already have a global validator)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "New password must include upper, lower, digit and special character"
    )
    private String newPassword;
}
