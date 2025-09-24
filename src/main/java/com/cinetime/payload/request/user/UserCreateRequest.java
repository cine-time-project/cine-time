package com.cinetime.payload.request.user;

import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    private String name;

    @NotBlank
    @Size(min = 3, max = 25)
    private String surname;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Pattern(regexp = "^\\(\\d{3}\\) \\d{3}-\\d{4}$")
    private String phoneNumber;

    @Past
    @NotNull
    private LocalDate birthDate;

    @NotNull
    private Gender gender;

    private boolean builtIn = false;

    private RoleName role = RoleName.MEMBER;

}

