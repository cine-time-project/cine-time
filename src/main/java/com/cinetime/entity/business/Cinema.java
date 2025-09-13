package com.cinetime.entity.business;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
    name = "cinemas",
    indexes = {
        @Index(name = "idx_cinema_city", columnList = "cityId")
    }
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

  // Cinema <-> City ManyToMany
  @ManyToMany(
      // When cinema is persisted or updated, city table will also be persisted or updated.
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      fetch = FetchType.LAZY //default behaviour
  )
  @JoinTable(
      name = "cinema_city",
      joinColumns = @JoinColumn(name = "cinema_id"),
      inverseJoinColumns = @JoinColumn(name = "city_id")
  )
  private Set<City> cities = new HashSet<>();

    @OneToMany(mappedBy = "cinema")
    private List<Hall> hall;

    @OneToMany(mappedBy = "cinema")
    private List<Favorite> favorites;

    @ManyToMany
    @JoinTable(name="movie_cinema",
    joinColumns = @JoinColumn(name = "cinema_id"),
    inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private List<Movie>movies;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

  @NotNull
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  //-------------------- LIFECYCLE --------------------
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
