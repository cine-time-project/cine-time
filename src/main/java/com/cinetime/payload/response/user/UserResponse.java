package com.cinetime.payload.response.user;

import java.time.LocalDate;


import lombok.Builder;
import lombok.Data;


@Data
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;

    public void setUsername(String updatedUser) {
    }
}