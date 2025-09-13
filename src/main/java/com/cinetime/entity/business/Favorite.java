package com.cinetime.entity.business;


import com.cinetime.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorite",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_favorite_user_movie",  columnNames = {"user_id", "movie_id"}),
                @UniqueConstraint(name = "uk_favorite_user_cinema", columnNames = {"user_id", "cinema_id"})
        },
        indexes = {
                @Index(name = "ix_favorite_user_id",   columnList = "user_id"),
                @Index(name = "ix_favorite_movie_id",  columnList = "movie_id"),
                @Index(name = "ix_favorite_cinema_id", columnList = "cinema_id")
        }
)
// DB-level XOR: exactly one of movie/cinema must be set (mirrors entity guard)
@Check(constraints = "(movie_id IS NOT NULL AND cinema_id IS NULL) OR (movie_id IS NULL AND cinema_id IS NOT NULL)")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user","movie","cinema"})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Owner (immutable after insert)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_favorite_user"))
    private User user;

    // Exactly one of movie or cinema must be set
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", updatable = false,
            foreignKey = @ForeignKey(name = "fk_favorite_movie"))
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", updatable = false,
            foreignKey = @ForeignKey(name = "fk_favorite_cinema"))
    private Cinema cinema;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // App-level XOR (fail-fast before hitting DB)
    @PrePersist @PreUpdate
    private void validateExactlyOneTarget() {
        boolean hasMovie = movie != null;
        boolean hasCinema = cinema != null;
        if (hasMovie == hasCinema) {
            throw new IllegalStateException("Exactly one of 'movie' or 'cinema' must be set for Favorite.");
        }
    }
}
