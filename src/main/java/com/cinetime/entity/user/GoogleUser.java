package com.cinetime.entity.user;

import com.cinetime.entity.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "google_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GoogleUser extends User {

    @Column(unique = true, nullable = false)
    private String googleId; // Google "sub" field

    @Column(columnDefinition = "TEXT")
    private String picture; // profile picture

    // --- Life Cycle ---
    @PrePersist
    protected void onCreateGoogleUser() {
        super.setProvider(AuthProvider.GOOGLE);
    }
}
