package com.cinetime.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorite",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "movie_id"},  name = "uk_favorite_user_movie"),
                @UniqueConstraint(columnNames = {"user_id", "cinema_id"}, name = "uk_favorite_user_cinema")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name="movie_id")
    private Long movieId;

    @Column(name="cinema_id")
    private Long cinemaId;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships for lazy loading/joins (non-insertable to avoid column duplication)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Cinema cinema;

    // Validation for schema CHECK constraint
    @PrePersist
    @PreUpdate
    private void validateConstraint() {
        boolean movie = movieId != null;
        boolean cinema = cinemaId != null;
        if (movie == cinema) {
            throw new IllegalArgumentException("Provide exactly one of movieId or cinemaId");
        }
    }


}
