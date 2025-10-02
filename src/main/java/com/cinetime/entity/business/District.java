package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class District {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(max = 30)
  @Column(nullable = false, length = 30, unique = true)
  private String name;

  @ManyToOne
  @JoinColumn(name = "city_id")
  private City city;

  @JsonIgnore
  @OneToMany(mappedBy = "district", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Set<Country> countries = new HashSet<>();
}
