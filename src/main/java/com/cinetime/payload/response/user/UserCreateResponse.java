package com.cinetime.payload.response.user;

import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.enums.RoleName;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateResponse {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private Gender gender;
    private boolean builtIn;
    private Set<RoleName> roles;

}