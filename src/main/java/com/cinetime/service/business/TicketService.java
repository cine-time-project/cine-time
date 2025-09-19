package com.cinetime.service.business;

import com.cinetime.payload.mappers.TicketMapper;
import com.cinetime.payload.request.BuyTicketRequest;
import com.cinetime.payload.response.business.TicketResponse;
import com.cinetime.repository.business.TicketRepository;
import com.cinetime.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final PageableHelper pageableHelper;
    private final TicketMapper ticketMapper;

    //T01

    public List<TicketResponse> getCurrentTickets(Long userId, Integer page, Integer size, String sort, String type) {
        // TODO: build Pageable and call your ticketRepository current query, then map
        Pageable pageable = (size == null || size <= 0) ? Pageable.unpaged() : PageRequest.of(page == null ? 0 : page, size);
        // TODO: return ticketMapper.toResponses(page.getContent());
        return List.of();
    }

    public List<TicketResponse> getPassedTickets(Long userId, Integer page, Integer size, String sort, String type) {
        // TODO: build Pageable and call your ticketRepository passed query, then map
        Pageable pageable = (size == null || size <= 0) ? Pageable.unpaged() : PageRequest.of(page == null ? 0 : page, size);
        return List.of();
    }

    public TicketResponse reserve(Long movieId, int count, Long maybeUserId) {
        // TODO: use showtimeRepository.findNextFutureShowtime(movieId, LocalDate.now(), LocalTime.now())
        // TODO: create RESERVED ticket(s) and return ticketMapper.mapTicketToTicketResponse(...)
        return null;
    }

    public TicketResponse buy(BuyTicketRequest req, Long maybeUserId) {
        // TODO: resolve showtime (movie/hall/cinema/date/startTime), validate not in past, check seats, create ticket(s)
        return null;
    }
}








