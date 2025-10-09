package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cinemaImages")
public class CinemaImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = true)
    private byte[] data;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String type;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne (fetch = FetchType.LAZY )
    @JoinColumn(name = "cinema_id", nullable = false,unique = true)
    @JsonIgnore
    private Cinema cinema;

    @Column(name = "url", length = 1024, nullable = true)
    private String url;
    @jakarta.validation.constraints.AssertTrue(message = "Either data or url must be present")
    private boolean isEitherDataOrUrlPresent() {
        return (data != null && data.length > 0) || (url != null && !url.isBlank());
    }
}
