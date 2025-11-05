package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "halls",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cinema_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Min(1)
    @Column(name = "seat_capacity", nullable = false)
    private Integer seatCapacity;

    @Builder.Default
    @Column(name = "is_special", nullable = false)
    private Boolean isSpecial = false;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "cinema_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_hall_cinema")
    )
    private Cinema cinema;

    // Hall -> Showtime: Cinema silinince Hall'lar; Hall silinince Showtime'lar da silinir
    @JsonIgnore
    @OneToMany(
            mappedBy = "hall",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<Showtime> showtimes = new LinkedHashSet<>();

    // -------------------- LIFECYCLE --------------------
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
