package com.cinetime.controller.business;

import com.cinetime.payload.request.business.BuyTicketRequest;
import com.cinetime.payload.request.business.ReserveTicketRequest;
import com.cinetime.payload.response.business.TicketResponse;
import com.cinetime.service.business.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

    @RestController
    @RequestMapping("/api/tickets")
    @RequiredArgsConstructor
    public class TicketController {
        private final TicketService ticketService;

        @GetMapping("/auth/current-tickets")
        @PreAuthorize("hasRole('MEMBER')")
        public List<TicketResponse> currentTickets(
                @AuthenticationPrincipal com.cinetime.security.service.UserDetailsImpl principal,
                @RequestParam(defaultValue = "0") Integer page,
                @RequestParam(defaultValue = "0") Integer size,
                @RequestParam(required = false) String sort,
                @RequestParam(defaultValue = "ASC") String type) {

            Long userId = principal.getId();
            return ticketService.getCurrentTickets(userId, page, size, sort, type);
        }

        @GetMapping("/auth/passed-tickets")
        @PreAuthorize("hasRole('MEMBER')")
        public List<TicketResponse> passedTickets(
                @AuthenticationPrincipal com.cinetime.security.service.UserDetailsImpl principal,
                @RequestParam(defaultValue = "0") Integer page,
                @RequestParam(defaultValue = "0") Integer size,
                @RequestParam(required = false) String sort,
                @RequestParam(defaultValue = "ASC") String type) {

            Long userId = principal.getId();
            return ticketService.getPassedTickets(userId, page, size, sort, type);
        }

        @PostMapping("/reserve")
        public List<TicketResponse> reserve(
                @AuthenticationPrincipal com.cinetime.security.service.UserDetailsImpl principal,
                @RequestBody ReserveTicketRequest req) {
            Long userId = principal != null ? principal.getId() : null; // allow guest if null
            return ticketService.reserve(req, userId);
        }

        @PostMapping("/buy-ticket")
        @ResponseStatus(HttpStatus.CREATED)
        public TicketResponse buy(
                @AuthenticationPrincipal com.cinetime.security.service.UserDetailsImpl principal,
                @RequestBody BuyTicketRequest request) {
            Long userId = principal != null ? principal.getId() : null;
            return ticketService.buy(request, userId);
        }
    }

