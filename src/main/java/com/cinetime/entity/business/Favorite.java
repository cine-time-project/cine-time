package com.cinetime.entity.business;


import com.cinetime.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
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

    // FK alanları (insert/update bunlar üzerinden)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Bir kayıtta sadece biri dolu olacak (DB CHECK ile garanti)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "cinema_id")
    private Long cinemaId;

    // İlişkisel erişimler (opsiyonel; sorgu/DTO için faydalı)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", insertable = false, updatable = false)
    private Cinema cinema;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Kolay kullanım için ek ctor
    public Favorite(Long userId, Long movieId, Long cinemaId) {
        this.userId = userId;
        this.movieId = movieId;
        this.cinemaId = cinemaId;
    }
}
