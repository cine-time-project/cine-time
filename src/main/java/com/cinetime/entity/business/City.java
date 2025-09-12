package com.cinetime.entity.business;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class City {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30, unique = true)
  private String name;

  @ManyToOne
  @JoinColumn(name = "country_id")
  private Country country;

  @OneToMany(
      mappedBy = "city",
      cascade = CascadeType.ALL, //All operation including REMOVE will be applied to the districts.
      orphanRemoval = true
  )
  private Set<District> districts;

  @ManyToMany(mappedBy = "cities", fetch = FetchType.LAZY)
  private Set<Cinema> cinemas = new HashSet<>();
}
