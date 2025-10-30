package com.cinetime.payload.response.user;

import com.cinetime.entity.enums.RoleName;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;
    private Set<RoleName> roles;

    public void setUsername(String updatedUser) {
    }
}