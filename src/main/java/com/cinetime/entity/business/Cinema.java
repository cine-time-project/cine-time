package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
    name = "cinemas"
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

    @JsonIgnore
    @OneToMany(mappedBy = "cinema")
    private List<Hall> hall;

    @JsonIgnore
    @OneToMany(
          mappedBy = "cinema",
          fetch = FetchType.LAZY,
          cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
          orphanRemoval = true
  )
  private Set<Favorite> favorites = new LinkedHashSet<>();



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
