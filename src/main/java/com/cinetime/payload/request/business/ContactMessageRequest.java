package com.cinetime.payload.request.business;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessageRequest {

    @NotBlank
    @Size(max = 120)
    private String fullName; // Ad Soyad

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;    // E-Posta

    @NotBlank
    @Pattern(regexp = "^\\(\\d{3}\\) \\d{3}-\\d{4}$",
            message = "Telefon formatı '(XXX) XXX-XXXX' olmalıdır.")
    private String phoneNumber; // Telefon

    @NotBlank
    @Size(max = 160)
    private String subject;  // Konu

    @NotBlank
    @Size(max = 5000)
    private String message;  // Mesaj

    // getters/setters...
}