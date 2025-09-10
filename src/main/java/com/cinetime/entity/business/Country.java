package com.cinetime.entity.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

  // TODO: 30 karakter sınırlaması bu şekilde yalnız DB'de mi yapılmalı
  //  yoksa RequestDTO'sunda @Size ile de mi yapılmalı
  @Column(nullable = false, length = 30)
  private String name;
}
