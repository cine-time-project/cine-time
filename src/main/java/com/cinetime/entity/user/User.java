package com.cinetime.entity.user;

import com.cinetime.entity.business.Favorite;
import com.cinetime.entity.business.Payment;
import com.cinetime.entity.business.Role;
import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.AuthProvider;
import com.cinetime.entity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED) // GoogleUser will extend from here
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 25)
    private String surname;

    @JsonIgnore
    @Column(nullable = true) // for GoogleUser it may be null.
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true) // for GoogleUser it may be null.
    private String phoneNumber;

    @Column(nullable = true) // for GoogleUser it may be null.
    private LocalDate birthDate;

    @Column(nullable = true) // for GoogleUser it may be null.
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private Boolean builtIn = false;

    @Column(nullable = true)
    @JsonIgnore
    private String resetPasswordCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "userrole",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Ticket> tickets = new HashSet<>();


    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Favorite> favorites = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @OrderBy("paymentDate DESC")
    private List<Payment> payments = new ArrayList<>();

    // --- Life Cycle ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
