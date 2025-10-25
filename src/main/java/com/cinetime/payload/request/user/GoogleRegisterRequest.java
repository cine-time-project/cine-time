package com.cinetime.payload.request.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoogleRegisterRequest extends UserRegisterRequest {

    private String googleId; // Google "sub" field

    private String picture; // profile picture


    /* fields that come from Parent
    -------------------------------------
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String password;
    private LocalDate birthDate;
    private Gender gender; // MALE, FEMALE, OTHER
    */

}
