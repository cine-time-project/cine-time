package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Ticket;
import com.cinetime.payload.response.business.TicketResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data

public class TicketMapper {
    //Ticket entity > TicketResponse DTO
    public TicketResponse mapTicketToTicketResponse(Ticket ticket){
        if (ticket ==null) return null;

        return TicketResponse.builder()
                .id(ticket.getId())
                .movieName(ticket.getShowtime().getMovie().getTitle())
                .cinema(ticket.getShowtime().getHall().getCinema().getName())
                .hall(ticket.getShowtime().getHall().getName())
                .date(ticket.getShowtime().getDate())
                .startTime(ticket.getShowtime().getStartTime())
                .seatLetter(ticket.getSeatLetter())
                .seatNumber(ticket.getSeatNumber())
                .price(ticket.getPrice())
                .status(ticket.getStatus()).build();

    }

}
