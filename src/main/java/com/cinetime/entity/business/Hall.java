package com.cinetime.entity.business;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "halls", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cinema_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(name = "seat_capacity", nullable = false)
    private Integer seatCapacity;

    @Builder.Default
    @Column(name = "is_special", nullable = false)
    private Boolean isSpecial = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //    // Foreign Key Field - Görseldeki kırmızı nota göre eklendi
//    @NotNull
//    @Column(name = "cinema_id", nullable = false)
//    private Long cinemaId; // Foreign Key to CINEMA
    //Buna gerek yokmuş.Şimdilik yoruma aldım.
    //Çünkü JPA’da foreign key alanını manuel tanımlamaya gerek yok, mapping üzerinden hallediliyor.

    // Entity Relationship - JPA mapping için
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", insertable = false, updatable = false)
    private Cinema cinema;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL)
    private Set<Showtime> showtimes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}