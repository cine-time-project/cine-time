package com.cinetime.payload.request.user;

import java.time.LocalDate;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;

}

