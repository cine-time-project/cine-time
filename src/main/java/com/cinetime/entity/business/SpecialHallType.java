package com.cinetime.entity.business;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "special_hall_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_special_hall_type_name", columnNames = "name")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialHallType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String name; // IMAX, 4DX…

    @Column(nullable=false, precision=5, scale=2)
    private BigDecimal priceDiffPercent; // ör: 15.00 => %15
}
