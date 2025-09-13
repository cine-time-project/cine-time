package com.cinetime.entity.business;

import com.cinetime.entity.enums.TicketStatus;
import com.cinetime.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tickets_showtime_seat",
                        columnNames = {"showtimeId", "seatLetter", "seatNumber"}
                )
        },
        indexes = {
                @Index(name = "idx_tickets_showtime", columnList = "showtimeId"),
                @Index(name = "idx_tickets_user",    columnList = "userId"),
                @Index(name = "idx_tickets_payment", columnList = "paymentId"),
                @Index(name = "idx_tickets_status",  columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "seatLetter", nullable = false, length = 5)
    private String seatLetter;

    @Min(1)
    @Column(name = "seatNumber", nullable = false)
    private int seatNumber;

    @NotNull
    @Column(name = "price", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.RESERVED;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    // -------------------- RELATIONS) --------------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtimeId", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_showtime"))
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId",
            foreignKey = @ForeignKey(name = "fk_ticket_user"))
    private User user;


    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "payment_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_ticket_payment"))
    private Payment payment;


    // -------------------- LIFECYCLE --------------------
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = TicketStatus.RESERVED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
