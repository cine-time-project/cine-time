package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "cinemas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cinema_city_name",
                columnNames = {"city_id", "name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Cinema {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 50)
  private String slug;

  // Cinema <-> City (ManyToMany): City bağımsız referans veri, cascade vermiyoruz
  // Cinema -> City (ManyToOne REQUIRED): each cinema (location) belongs to exactly one city
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cinema_city"))
  private City city;

  // Cinema -> Hall (OneToMany): Cinema silinirse Hall'lar da silinsin
  @JsonIgnore
  @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Hall> halls = new LinkedHashSet<>();

  // Cinema -> Favorite (OneToMany): Cinema silinince favoriler de silinsin
  @JsonIgnore
  @OneToMany(mappedBy = "cinema", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Set<Favorite> favorites = new LinkedHashSet<>();

  // Cinema <-> Movie (ManyToMany): Movie'ler bağımsız, cascade yok; join satırı unique
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
          name = "movie_cinema",
          joinColumns = @JoinColumn(
                  name = "cinema_id",
                  foreignKey = @ForeignKey(name = "fk_movie_cinema_cinema")
          ),
          inverseJoinColumns = @JoinColumn(
                  name = "movie_id",
                  foreignKey = @ForeignKey(name = "fk_movie_cinema_movie")
          ),
          uniqueConstraints = @UniqueConstraint(
                  name = "uk_movie_cinema",
                  columnNames = {"cinema_id", "movie_id"}
          )
  )
  private Set<Movie> movies = new LinkedHashSet<>();

  @NotNull
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @NotNull
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  // -------------------- LIFECYCLE --------------------
  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
