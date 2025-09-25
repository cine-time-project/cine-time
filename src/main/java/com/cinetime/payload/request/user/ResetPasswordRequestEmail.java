package com.cinetime.payload.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequestEmail {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp="^\\d{6}$", message="Code must be 6 digits")
    private String code; // 6-haneli

    // min 8, 1 büyük, 1 küçük, 1 sayı, 1 özel
    @NotBlank
    @Pattern(
            regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
            message="Password must contain upper, lower, digit and special char, min 8"
    )
    private String newPassword;
}
