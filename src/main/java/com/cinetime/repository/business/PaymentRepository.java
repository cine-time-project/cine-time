package com.cinetime.repository.business;


import com.cinetime.entity.business.Payment;
import com.cinetime.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long>, JpaSpecificationExecutor<Payment> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);



        @Query(
                value = """
      select distinct p
      from Payment p
      join p.tickets t
    """,
                countQuery = """
      select count(distinct p)
      from Payment p
      join p.tickets t
    """
        )
        @EntityGraph(attributePaths = "tickets")
        Page<Payment> findAllWithTickets(Pageable pageable);




    @Override
    @EntityGraph(attributePaths = "tickets")
    @org.springframework.lang.NonNull
    Page<Payment> findAll(
            @org.springframework.lang.Nullable Specification<Payment> spec,
            @org.springframework.lang.NonNull Pageable pageable
    );

    @Query("""
      select coalesce(sum(p.amount), 0)
      from Payment p
      where p.user.id = :userId and p.paymentStatus = :status
    """)
    BigDecimal sumByUserAndStatus(Long userId, PaymentStatus status);

    long countByUser_IdAndPaymentStatus(Long userId, PaymentStatus status);

    @Query("""
      select max(p.paymentDate)
      from Payment p
      where p.user.id = :userId and p.paymentStatus = :status
    """)
    LocalDateTime lastAtByUserAndStatus(Long userId, PaymentStatus status);
}
