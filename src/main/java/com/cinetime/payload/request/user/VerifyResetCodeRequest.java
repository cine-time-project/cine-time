package com.cinetime.payload.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResetCodeRequest {

    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotBlank(message = "Verification code must not be blank")
    private String code;
}
