package com.cinetime.service.business;

import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.TicketStatus;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.TicketMapper;
import com.cinetime.payload.request.business.BuyTicketRequest;
import com.cinetime.payload.request.business.ReserveTicketRequest;
import com.cinetime.payload.response.business.TicketResponse;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.business.TicketRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final PageableHelper pageableHelper;
    private final TicketMapper ticketMapper;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;

    //T01
    @Transactional(readOnly = true)
    public Page<TicketResponse> getCurrentTickets(Long userId, Integer page, Integer size, String sort, String type) {
        Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);

        var statuses = List.of(TicketStatus.PAID, TicketStatus.RESERVED);

        var today = java.time.LocalDate.now();
        var now   = java.time.LocalTime.now();

        Page<Ticket> result = ticketRepository.findCurrentForUser(userId, statuses, today, now, pageable);

        return result.map(ticketMapper::mapTicketToTicketResponse);
    }


    @Transactional(readOnly = true)
    public Page<TicketResponse> getPassedTickets(Long userId, Integer page, Integer size, String sort, String type) {
        Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);

        var today = java.time.LocalDate.now();
        var now   = java.time.LocalTime.now();

        Page<Ticket> passed = ticketRepository.findPassedForUserAt(
                userId,
                TicketStatus.USED,
                TicketStatus.PAID,
                today,
                now,
                pageable
        );

        return passed.map(ticketMapper::mapTicketToTicketResponse);
    }

    // RESERVE — also uses movieName + hall + cinema + date + showtime (LocalTime)
// returns one TicketResponse per seat reserved
    @Transactional
    public List<TicketResponse> reserve(ReserveTicketRequest req, Long maybeUserId) {
        // 1) Resolve showtime by MOVIE TITLE (not ID)
        var showtime = showtimeRepository
                .findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                        req.getMovieName(),
                        req.getHall(),
                        req.getCinema(),
                        req.getDate(),
                        req.getShowtime()
                )
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        // 2) Ensure the showtime is in the future (local machine clock)
        var today = java.time.LocalDate.now();
        var now   = java.time.LocalTime.now();
        boolean isFuture =
                showtime.getDate().isAfter(today) ||
                        (showtime.getDate().isEqual(today) && showtime.getStartTime().isAfter(now));
        if (!isFuture) {
            throw new IllegalArgumentException("Showtime is already passed, please try to reserve a upcoming showtime");
        }

        // 3) Load user
        var user = userRepository.findById(maybeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 4) Build tickets (one per requested seat) after availability check
        var ticketsToCreate = new java.util.ArrayList<Ticket>();
        for (var seat : req.getSeatInformation()) { // <<< was getSeats()
            String seatLetter = seat.getSeatLetter();
            int seatNumber    = seat.getSeatNumber();

            boolean taken = ticketRepository
                    .existsByShowtime_IdAndSeatLetterAndSeatNumber(showtime.getId(), seatLetter, seatNumber);
            if (taken) {
               // throw new IllegalStateException("Seat " + seatLetter + "-" + seatNumber + " is already reserved/paid");
                throw new ConflictException("Seat " + seatLetter + "-" + seatNumber + " is already reserved/paid");
            }

            Ticket ticket = new Ticket();
            ticket.setShowtime(showtime);
            ticket.setUser(user);
            ticket.setSeatLetter(seatLetter);
            ticket.setSeatNumber(seatNumber);
            ticket.setStatus(TicketStatus.RESERVED);
            ticket.setPrice(9.99); // TODO set real price

            ticketsToCreate.add(ticket);
        }

        // 5) Persist and map
        var saved = ticketRepository.saveAll(ticketsToCreate);
        return saved.stream().map(ticketMapper::mapTicketToTicketResponse).toList();
    }
    @Transactional
    public List<TicketResponse> buy(BuyTicketRequest ticketRequest, Long maybeUserId) {
        Showtime showtime = showtimeRepository
                .findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                        ticketRequest.getMovieName(),
                        ticketRequest.getHall(),
                        ticketRequest.getCinema(),
                        ticketRequest.getDate(),
                        ticketRequest.getShowtime()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found"));

        var today = java.time.LocalDate.now();
        var now   = java.time.LocalTime.now();

        // 3) Load user (if your Ticket.user is non-nullable, this must be set)
        var user = userRepository.findById(maybeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 4) Build tickets (one per seat) with status = PAID after checking availability
        var toCreate = new java.util.ArrayList<Ticket>();
        for (var seat : ticketRequest.getSeatInformation()) {
            String seatLetter = seat.getSeatLetter();
            int seatNumber    = seat.getSeatNumber();

            // IMPORTANT: prefer the relation-traversal form with an underscore in 'Showtime_Id'
            boolean taken = ticketRepository
                    .existsByShowtime_IdAndSeatLetterAndSeatNumber(showtime.getId(), seatLetter, seatNumber);
            if (taken) {
                throw new ConflictException("Seat " + seatLetter + "-" + seatNumber + " is already reserved/paid");
            }

            Ticket ticket = new Ticket();
            ticket.setShowtime(showtime);
            ticket.setUser(user);
            ticket.setSeatLetter(seatLetter);
            ticket.setSeatNumber(seatNumber);
            ticket.setStatus(TicketStatus.PAID);

            // TODO: assign a real price. Ticket.price is @NotNull in your entity, so set something valid.
            // You can compute based on hall/format, or temporarily hardcode for now:
            ticket.setPrice(9.99);

            toCreate.add(ticket);
        }

        // 5) Persist and map (return all created tickets)
        var saved = ticketRepository.saveAll(toCreate);
        return saved.stream()
                .map(ticketMapper::mapTicketToTicketResponse)
                .toList();
    }
}
