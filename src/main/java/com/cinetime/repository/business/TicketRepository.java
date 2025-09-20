package com.cinetime.repository.business;

import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {

    Page<Ticket> findByUserIdAndStatusIn(Long userId, List<TicketStatus> statuses, Pageable pageable);

    boolean existsByShowtime_IdAndSeatLetterAndSeatNumber(Long showtimeId, String seatLetter, int seatNumber);




    // Current (future) tickets for a user
    @Query("""
              select t from Ticket t
              where t.user.id = :userId
                and (t.status in :statuses)
                and (
                      t.showtime.date > CURRENT_DATE
                   or (t.showtime.date = CURRENT_DATE and t.showtime.startTime >= CURRENT_TIME)
                )
              order by t.showtime.date asc, t.showtime.startTime asc
            """)
    Page<Ticket> findCurrentForUser(@Param("userId") Long userId,
                                    @Param("statuses") List<TicketStatus> statuses,
                                    Pageable pageable);

    // Passed (past/used) tickets for a user
    @Query("""
              select t from Ticket t
              where t.user.id = :userId
                and (
                     t.status = :used
                  or (
                       t.status = :purchased
                   and (
                         t.showtime.date < CURRENT_DATE
                      or (t.showtime.date = CURRENT_DATE and t.showtime.startTime < CURRENT_TIME)
                      )
                  )
                )
              order by t.showtime.date desc, t.showtime.startTime desc
            """)
    Page<Ticket> findPassedForUser(@Param("userId") Long userId,
                                   @Param("used") com.cinetime.entity.enums.TicketStatus used,
                                   @Param("purchased") com.cinetime.entity.enums.TicketStatus purchased,
                                   Pageable pageable);

}
