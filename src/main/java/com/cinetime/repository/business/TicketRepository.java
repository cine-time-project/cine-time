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
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {

    Page<Ticket> findByUserIdAndStatusIn(Long userId, List<TicketStatus> statuses, Pageable pageable);

    boolean existsByShowtime_IdAndSeatLetterAndSeatNumber(Long showtimeId, String seatLetter, int seatNumber);




    // Current (future) tickets for a user
//    @Query("""
//              select t from Ticket t
//              where t.user.id = :userId
//                and (t.status in :statuses)
//                and (
//                      t.showtime.date > CURRENT_DATE
//                   or (t.showtime.date = CURRENT_DATE and t.showtime.startTime >= CURRENT_TIME)
//                )
//              order by t.showtime.date asc, t.showtime.startTime asc
//            """)
//    Page<Ticket> findCurrentForUser(@Param("userId") Long userId,
//                                    @Param("statuses") List<TicketStatus> statuses,
//                                    Pageable pageable);

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


}
