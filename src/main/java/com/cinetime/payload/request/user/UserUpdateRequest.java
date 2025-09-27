package com.cinetime.payload.request.user;

import com.cinetime.entity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {

    // UserUpdateRequest.java
        @Size(min = 2, max = 40) private String firstName;
        @Size(min = 2, max = 40) private String lastName;

        @Pattern(regexp = "^\\(\\d{3}\\)\\s\\d{3}-\\d{4}$")
        private String phone;


        @Email private String email;

    // (XXX) XXX-XXXX
    @Pattern(regexp = "^\\(\\d{3}\\)\\s\\d{3}-\\d{4}$", message = "Phone must be in form of (XXX) XXX-XXXX")
    private String phone;


        @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$")
        private String password;


        @Past
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

    // 8+ char, 1 upper, 1 lower, 1 digit, 1 special
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
            message = "Password must contain upper, lower, digit and special char, min 8")
    private String password;


        private Gender gender;


        // --- (for mapper 'name/surname/phoneNumber' ) ---
        public String getName() { return firstName; }
        public String getSurname() { return lastName; }
        public String getPhoneNumber() { return phone; }

        public void setName(String v) { this.firstName = v; }
        public void setSurname(String v) { this.lastName = v; }
        public void setPhoneNumber(String v) { this.phone = v; }
    }

    private Gender gender; // MALE, FEMALE, OTHER
}

