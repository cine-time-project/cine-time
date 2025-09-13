package com.cinetime.entity.business;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "image",
        indexes = {
                @Index(name = "ix_image_movie_id", columnList = "movie_id"),
                @Index(name = "ix_image_poster",   columnList = "is_poster")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "movie")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // File name or path (avoid BLOB for scalability)
    @Column(nullable = false, length = 255)
    private String name;

    // MIME type (e.g., image/png)
    @Column(length = 100)
    private String type;

    // True = poster (DB enforces one poster per movie via partial UNIQUE index)
    @Column(name = "is_poster", nullable = false)
    private boolean isPoster = false;

    // Owning movie (required)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_image_movie"))
    private Movie movie;

    // Auto-managed timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
