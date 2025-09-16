package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
                @Index(name = "ix_image_poster", columnList = "is_poster")
        }
)
@Getter
@Setter
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

    // Raw image bytes (PostgreSQL: BYTEA)
    @Lob
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] data;

    // Original file name
    @Column(nullable = false)
    private String name;

    // MIME type (e.g., image/jpeg)
    @Column
    private String type;

    // Owning Movie (many images per movie)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnore // prevent recursion when serializing Image
    private Movie movie;

    // Mark as poster (only one poster per movie enforced via DB partial unique index)
    @Column(name = "is_poster", nullable = false)
    private boolean isPoster = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
