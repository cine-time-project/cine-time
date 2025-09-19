package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"data", "movie"})
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Raw bytes of the image (BYTEA in PostgreSQL). */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] data;

    /** Original filename (e.g., poster.jpg). */
    @Column(nullable = false, length = 255)
    private String name;

    /** MIME type (nullable by schema): image/jpeg, image/png, image/webp... */
    @Column(length = 100)
    private String type;

    /** Owning Movie (required). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Movie movie;

    /** Poster flag (default false). DB should ensure only one true per movie. */
    @Column(name = "is_poster", nullable = false)
    private boolean isPoster = false;

    /** Creation timestamp (set by Hibernate). */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Update timestamp (set by Hibernate). */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
