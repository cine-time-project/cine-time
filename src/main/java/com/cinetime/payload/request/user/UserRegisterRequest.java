package com.cinetime.payload.request.user;

import com.cinetime.entity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegisterRequest {

    @NotBlank
    @Size(min = 2, max = 40)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 40)
    private String lastName;

    // (XXX) XXX-XXXX
    @NotBlank
    @Pattern(regexp = "^\\(\\d{3}\\)\\s\\d{3}-\\d{4}$",
            message = "Phone must be in form of (XXX) XXX-XXXX")
    private String phone;

    @NotBlank
    @Email
    private String email;

    // 8+ char, 1 upper, 1 lower, 1 digit, 1 special
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
            message = "Password must contain upper, lower, digit and special char, min 8")
    private String password;

    @NotNull
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull
    private Gender gender; // MALE, FEMALE, OTHER
}
