package com.cinetime.payload.request.user;

import com.cinetime.entity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "provider" // JSON içindeki provider alanına bakacak
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserRegisterRequest.class, name = "LOCAL"),
        @JsonSubTypes.Type(value = GoogleRegisterRequest.class, name = "GOOGLE")
})
public class UserRegisterRequest {

    //provider only exists in json payload that comes from FrontEnd to decide which subType of this class will be mapped.

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
