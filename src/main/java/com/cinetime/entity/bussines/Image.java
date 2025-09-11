package com.cinetime.entity.business;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "image",
        indexes = {
                @Index(name = "ix_image_movie_id", columnList = "movie_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dosyanın saklandığı path veya URL
    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    // MIME type: image/png, image/jpeg...
    @Column(name = "type", length = 100)
    private String type;

    // Poster flag (aynı film için sadece 1 true olabilir)
    @Column(name = "is_poster", nullable = false)
    private boolean isPoster = false;

    // FK alanı
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    // İlişkisel erişim (opsiyonel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    private Movie movie;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
