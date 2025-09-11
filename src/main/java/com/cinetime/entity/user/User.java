package com.cinetime.entity.user;

import com.cinetime.entity.business.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @NonNull
    @Size(min=3, max=20)
    private String name;

    @NotBlank
    @Size(min = 3, max = 25)
    private String surname;

    @NotNull
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @Pattern(regexp = "^\\(\\d{3}\\) \\d{3}-\\d{4}$")
    private String phoneNumber;

    @Past
    @NotNull
    private LocalDate birthDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Boolean builtIn = false;

    @NotNull
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-mm-yyyy")
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-mm-yyyy")
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private String resetPasswordCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    
}

