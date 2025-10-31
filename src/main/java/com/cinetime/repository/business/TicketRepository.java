package com.cinetime.repository.business;

import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {

    Page<Ticket> findByUserIdAndStatusIn(Long userId, List<TicketStatus> statuses, Pageable pageable);

    boolean existsByShowtime_IdAndSeatLetterAndSeatNumber(Long showtimeId, String seatLetter, int seatNumber);

    List<Ticket> findAllByPaymentId(Long id);

    @Query("""
        select concat(t.seatLetter, t.seatNumber)
        from Ticket t
        where t.showtime.id = :showtimeId
          and t.status in (com.cinetime.entity.enums.TicketStatus.PAID,
                           com.cinetime.entity.enums.TicketStatus.RESERVED)
    """)
    List<String> findTakenSeatIds(@Param("showtimeId") Long showtimeId);


    long countByShowtime_IdAndStatusIn(Long showtimeId, Collection<TicketStatus> statuses);


    @Query("""
           select t
           from Ticket t
           join t.showtime st
           where t.user.id = :userId
             and t.status in :statuses
             and (
                   st.date > :todayDate
                or (st.date = :todayDate and st.startTime > :currentTime)
             )
           order by st.date asc, st.startTime asc
           """)
    Page<Ticket> findCurrentForUser(@Param("userId") Long userId,
                                      @Param("statuses") List<TicketStatus> statuses,
                                      @Param("todayDate") LocalDate todayDate,
                                      @Param("currentTime") LocalTime currentTime,
                                      Pageable pageable);

    // Passed (past/used) tickets for a user
    @Query("""
           select t
           from Ticket t
           join t.showtime st
           where t.user.id = :userId
             and (
                  t.status = :usedStatus
               or (
                    t.status = :paidStatus
                and (
                      st.date < :todayDate
                   or (st.date = :todayDate and st.startTime <= :currentTime)
                    )
                  )
                )
           order by st.date desc, st.startTime desc
           """)
    Page<Ticket> findPassedForUserAt(@Param("userId") Long userId,
                                     @Param("usedStatus") TicketStatus usedStatus,
                                     @Param("paidStatus") TicketStatus paidStatus,
                                     @Param("todayDate") LocalDate todayDate,
                                     @Param("currentTime") LocalTime currentTime,
                                     Pageable pageable);


    boolean existsByShowtimeId(Long id);

    boolean existsByShowtime_Id(Long showtimeId);
    long countByShowtime_Id(Long showtimeId);
}
