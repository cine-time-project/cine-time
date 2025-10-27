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

 @ManyToOne(fetch = FetchType.LAZY,optional = false)
  @JoinColumn(name = "city_id",nullable = false, foreignKey = @ForeignKey(name = "fk_city_district"))
  private City city;


}
