package com.cinetime.entity.business;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;           // Ad Soyad

    @Column(name = "email", nullable = false, length = 160)
    private String email;              // E-Posta

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;        // Telefon (örn: (312) 813-2564)

    @Column(name = "subject", nullable = false, length = 160)
    private String subject;            // Konu

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;            // Mesaj

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;  // kaydetme zamanı

    /* getters & setters */

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    // getters/setters...
}
