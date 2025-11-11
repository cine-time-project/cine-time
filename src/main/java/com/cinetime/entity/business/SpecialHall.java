package com.cinetime.entity.business;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "special_halls",
        uniqueConstraints = @UniqueConstraint(name="uk_special_hall_hall_id", columnNames="hall_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialHall {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Her SpecialHall bir gerçek salona (Hall) işaret eder
    @OneToOne(optional = false)
    @JoinColumn(name = "hall_id", nullable=false,
            foreignKey = @ForeignKey(name="fk_special_hall_hall"))
    private Hall hall;

    // Seçilen tip (IMAX vs.)
    @ManyToOne(optional = false)
    @JoinColumn(name="type_id", nullable=false,
            foreignKey=@ForeignKey(name="fk_special_hall_type"))
    private SpecialHallType type;
}
