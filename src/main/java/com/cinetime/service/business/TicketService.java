package com.cinetime.service.business;

import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.TicketStatus;
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

import java.time.LocalDate;
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

    public List<TicketResponse> getCurrentTickets(Long userId, Integer page, Integer size, String sort, String type) {
       Pageable pageable = pageableHelper.buildPageable(page,size,sort,type);

       //We want to return the current tickets, which means ticket status can only be PAID and RESERVED,
        //USED and CANCELLED tickets are not current, therefore we are creating the list below to filter against statuses.
       var statuses = List.of(TicketStatus.PAID, TicketStatus.RESERVED);
       //Calling the ticketRepository below to return the current tickets
        Page<Ticket> result= ticketRepository.findCurrentForUser(userId,statuses,pageable);
        List<TicketResponse> currentTickets= result.getContent().stream()
                .map(ticketMapper::mapTicketToTicketResponse).toList();
        return currentTickets;


    }

    public List<TicketResponse> getPassedTickets(Long userId, Integer page, Integer size, String sort, String type) {
      Pageable pageable = pageableHelper.buildPageable(page,size,sort,type);
      Page<Ticket> passedTickets= ticketRepository.findPassedForUser(
              userId,
              TicketStatus.USED,
              TicketStatus.CANCELLED,
              pageable
      );
      return passedTickets.getContent().stream().map(ticketMapper::mapTicketToTicketResponse).toList();


    }

    public List<TicketResponse> reserve(ReserveTicketRequest req, Long maybeUserId) {
        var showtime = showtimeRepository
                .findByMovie_IdAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                        req.getMovieId(),
                        req.getHall(),
                        req.getCinema(),
                        req.getDate(),
                        req.getStartTime()
                )
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));
        // 1) Make sure the showtime is in the future (date > today OR same-day with startTime > now)
        var today = java.time.LocalDate.now();
        var now   = java.time.LocalTime.now();
        boolean isFuture =
                showtime.getDate().isAfter(today) ||
                        (showtime.getDate().isEqual(today) && showtime.getStartTime().isAfter(now));
        if (!isFuture) {
            throw new IllegalArgumentException("Showtime is not in the future");
        }

// 2) Build tickets (one per requested seat) after checking availability
        var ticketsToCreate = new java.util.ArrayList<Ticket>();
        for (var seat : req.getSeats()) {
            String seatLetter = seat.getSeatLetter();
            int seatNumber    = seat.getSeatNumber();

            // seat already taken?
            boolean taken = ticketRepository
                    .existsByShowtime_IdAndSeatLetterAndSeatNumber(showtime.getId(), seatLetter, seatNumber);
            if (taken) {
                throw new IllegalStateException("Seat " + seatLetter + "-" + seatNumber + " is already reserved/paid");
            }

            Ticket ticket = new Ticket();
            ticket.setShowtime(showtime);
            ticket.setSeatLetter(seatLetter);
            ticket.setSeatNumber(seatNumber);
            ticket.setStatus(TicketStatus.RESERVED);

            // TODO: set the real price (entity likely has @NotNull Double price)
            ticket.setPrice(0.0);

            // TODO: if Ticket.user is non-nullable, load and set the user using maybeUserId
            var user = userRepository.findById(maybeUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            ticket.setUser(user);

            ticketsToCreate.add(ticket);
        }

// 3) Persist and map
        var saved = ticketRepository.saveAll(ticketsToCreate);
        return saved.stream()
                .map(ticketMapper::mapTicketToTicketResponse)
                .toList();
    }

    public TicketResponse buy(BuyTicketRequest ticketRequest, Long maybeUserId) {
        Showtime showtime = showtimeRepository
                .findByMovie_IdAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                       ticketRequest.getMovieId(),
                        ticketRequest.getHall(),
                        ticketRequest.getCinema(),
                        LocalDate.now(),
                        ticketRequest.getShowtime()
                )
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        // 3) Load user (if your Ticket.user is non-nullable, this must be set)
        var user = userRepository.findById(maybeUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 4) Build tickets (one per seat) with status = PAID after checking availability
        var toCreate = new java.util.ArrayList<Ticket>();
        for (var seat : ticketRequest.getSeatInformation()) {
            String seatLetter = seat.getSeatLetter();
            int seatNumber    = seat.getSeatNumber();

            // IMPORTANT: prefer the relation-traversal form with an underscore in 'Showtime_Id'
            boolean taken = ticketRepository
                    .existsByShowtime_IdAndSeatLetterAndSeatNumber(showtime.getId(), seatLetter, seatNumber);
            if (taken) {
                throw new IllegalStateException("Seat " + seatLetter + "-" + seatNumber + " is already reserved/paid");
            }

            Ticket ticket = new Ticket();
            ticket.setShowtime(showtime);
            ticket.setUser(user);
            ticket.setSeatLetter(seatLetter);
            ticket.setSeatNumber(seatNumber);
            ticket.setStatus(TicketStatus.PAID);

            // TODO: assign a real price. Ticket.price is @NotNull in your entity, so set something valid.
            // You can compute based on hall/format, or temporarily hardcode for now:
            ticket.setPrice(0.0);

            toCreate.add(ticket);
        }

        // 5) Persist and map
        var saved = ticketRepository.saveAll(toCreate);

        // Your method returns a single TicketResponse. If multiple seats were purchased,
        // return the first one for now (or change the method to return List<TicketResponse>).
        Ticket first = saved.get(0);
        return ticketMapper.mapTicketToTicketResponse(first);
    }
    }









