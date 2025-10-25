package com.cinetime.payload.request.user;

import com.cinetime.entity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
public class GoogleRegisterRequest {

    private String googleId; // Google "sub" field

    private String picture; // profile picture

    @NotBlank
    @Size(min = 2, max = 40)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 40)
    private String lastName;

    @NotBlank
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
