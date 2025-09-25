package com.cinetime.payload.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestEmail {

    @NotBlank
    @Email
    private String email;

}
