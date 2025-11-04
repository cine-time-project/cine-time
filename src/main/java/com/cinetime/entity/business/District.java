package com.cinetime.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "city")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_district_city_name",
        columnNames = {"city_id", "name"}
))
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

  @NotNull
  @Size(max = 30)
  @Column(nullable = false, length = 30 )
  private String name;

 @ManyToOne(fetch = FetchType.LAZY,optional = false)
  @JoinColumn(name = "city_id",nullable = false, foreignKey = @ForeignKey(name = "fk_city_district"))
  private City city;


}
