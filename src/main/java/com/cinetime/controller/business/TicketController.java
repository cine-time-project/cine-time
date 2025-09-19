package com.cinetime.controller.business;

import com.cinetime.payload.request.BuyTicketRequest;
import com.cinetime.payload.response.business.TicketResponse;
import com.cinetime.service.business.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

    @RestController
    @RequestMapping("/api/tickets")
    @RequiredArgsConstructor
    public class TicketController {
        private final TicketService ticketService;

        @GetMapping("/auth/current-tickets")
        @PreAuthorize("hasRole('MEMBER')")
        public List<TicketResponse> currentTickets(
                @RequestParam(defaultValue = "0") Integer page,
                @RequestParam(defaultValue = "0") Integer size,
                @RequestParam(required = false) String sort,
                @RequestParam(defaultValue = "ASC") String type) {
            Long userId = /* TODO: extract from SecurityContext */ 1L;
            return ticketService.getCurrentTickets(userId, page, size, sort, type);
        }

        @GetMapping("/auth/passed-tickets")
        @PreAuthorize("hasRole('MEMBER')")
        public List<TicketResponse> passedTickets(
                @RequestParam(defaultValue = "0") Integer page,
                @RequestParam(defaultValue = "0") Integer size,
                @RequestParam(required = false) String sort,
                @RequestParam(defaultValue = "ASC") String type) {
            Long userId = /* TODO: extract from SecurityContext */ 1L;
            return ticketService.getPassedTickets(userId, page, size, sort, type);
        }

        @GetMapping("/reserve/{movieId}")
        public TicketResponse reserve(@PathVariable Long movieId,
                                      @RequestParam(defaultValue = "1") int count) {
            Long userId = /* optional */ null;
            return ticketService.reserve(movieId, count, userId);
        }

        @PostMapping("/buy-ticket")
        @ResponseStatus(HttpStatus.CREATED)
        public TicketResponse buy(@RequestBody BuyTicketRequest request) {
            Long userId = /* optional */ null;
            return ticketService.buy(request, userId);
        }
    }

