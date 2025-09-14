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

    // Original file name (as uploaded by the user)
    @Column(nullable = false, length = 255)
    private String name;

    // Absolute or relative file system path where the image is stored
    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    // MIME type of the image (e.g., image/png, image/jpeg)
    @Column(length = 100)
    private String type;

    // Indicates whether this image is the movie's poster
    // Only one poster per movie is allowed (enforced via partial unique index)
    @Column(name = "is_poster", nullable = false)
    private boolean isPoster = false;

    // Owning movie (each image belongs to a single movie)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_image_movie"))
    private Movie movie;

    // Creation timestamp (auto-managed)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Last update timestamp (auto-managed)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
