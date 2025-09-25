package com.cinetime.entity.business;

import com.cinetime.entity.enums.MovieStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @NotNull
    @Size(min = 5, max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String slug;

    @NotNull
    @Size(min = 3, max = 300)
    private String summary;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    private Integer duration;

    @Column(nullable = true)
    private Double rating;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private String specialHalls;

    @Column(nullable = true)
    @Size(min = 5, max = 20)
    private String director;

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "movie_cast",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "actor", nullable = false)
    private List<String> cast = new ArrayList<>();

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "movie_format",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "format", nullable = false)
    private List<String> formats = new ArrayList<>();

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "genre", nullable = false)
    private List<String> genre;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private MovieStatus status;

    @JsonIgnore
    @ManyToMany(mappedBy = "movies", fetch = FetchType.LAZY)
    private Set<Cinema> cinemas = new LinkedHashSet<>();

    // Movie -> Showtime : Movie silinince bağlı seanslar da silinsin
    @JsonIgnore
    @OneToMany(mappedBy = "movie",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<Showtime> showtimes = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "movie",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<Image> images = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
