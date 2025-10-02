package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Country {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(max = 30)
  @Column(nullable = false, length = 30, unique = true)
  private String name;

  @JsonIgnore
  @OneToMany(mappedBy = "country", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Set<City> cities = new HashSet<>();

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "district_id", nullable = false, foreignKey = @ForeignKey(name = "fk_country_district"))
  private District district;
}