package com.cinetime.entity.user;

import com.cinetime.entity.business.Favorite;
import com.cinetime.entity.business.Payment;
import com.cinetime.entity.business.Role;
import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @NotBlank
  @NotNull
  @Size(min = 3, max = 20)
  private String name;

  @NotBlank
  @Size(min = 3, max = 25)
  private String surname;

  @NotNull
  @JsonIgnore
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
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private LocalDateTime createdAt;

  @NotNull
  @Column(nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private LocalDateTime updatedAt;

  @Column(nullable = true)
  @JsonIgnore
  private String resetPasswordCode;


  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "userrole",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Ticket> tickets = new HashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Payment> payments = new HashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Favorite> favorites = new HashSet<>();


  // --- Life Cycle ---
  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


}
