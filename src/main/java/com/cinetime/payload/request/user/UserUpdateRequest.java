package com.cinetime.payload.request.user;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UserUpdateRequest {

    // UserUpdateRequest.java
    @Size(min = 2, max = 40) private String firstName;
    @Size(min = 2, max = 40) private String lastName;

    @Pattern(
            regexp = "^(\\+?[1-9]\\d{7,14}|\\(\\d{3}\\)\\s\\d{3}-\\d{4})$",
            message = "Phone must be E.164 (e.g. +491234567890) or (123) 456-7890"
    )
    private String phone;

    @Email private String email;

    @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$")
    private String password;

    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Gender gender;

    private Set<String> roles;

    // --- (for mapper 'name/surname/phoneNumber' ) ---
    public String getName() { return firstName; }
    public String getSurname() { return lastName; }
    public String getPhoneNumber() { return phone; }


    public void setName(String v) { this.firstName = v; }
    public void setSurname(String v) { this.lastName = v; }
    public void setPhoneNumber(String v) { this.phone = v; }

}
