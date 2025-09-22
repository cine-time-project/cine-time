package com.cinetime.controller.business;

import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.BuyTicketRequest;
import com.cinetime.payload.request.business.ReserveTicketRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.business.TicketResponse;
import com.cinetime.security.service.UserDetailsImpl;
import com.cinetime.service.business.TicketService;
import com.cinetime.service.helper.PageableHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

    @RestController
    @RequestMapping("/api/tickets")
    @RequiredArgsConstructor
    public class TicketController {
        private final TicketService ticketService;

        @Operation(
                summary = "Retrieve the tickets for the user which are not USED or CANCELLED",
                description = "Returns a paginated list of movie tickets that are not USED or CANCELLED"
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved current ticket list"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        })


       // @PreAuthorize("hasRole('MEMBER')")
        @GetMapping("/auth/current-tickets")
        @PreAuthorize("permitAll()")
        public ResponseEntity<ResponseMessage<Page<TicketResponse>>> currentTickets(
                @AuthenticationPrincipal UserDetailsImpl principal,
                @RequestParam(defaultValue = "0") Integer page,
                @RequestParam(defaultValue = "0") Integer size,
                @RequestParam(required = false) String sort,
                @RequestParam(defaultValue = "ASC") String type) {

            Long userId = principal.getId();
            Page<TicketResponse> body = ticketService.getCurrentTickets(userId, page, size, sort, type);

            ResponseMessage<Page<TicketResponse>> response = ResponseMessage.<Page<TicketResponse>>builder()
                    .returnBody(body)
                    .message(SuccessMessages.CURRENT_TICKETS_LISTED)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return ResponseEntity.ok(response);
        }

     //   @PreAuthorize("hasRole('MEMBER')")
        @PreAuthorize("permitAll()")
        @GetMapping("/auth/passed-tickets")

        public ResponseEntity<ResponseMessage<Page<TicketResponse>>> passedTickets(
                @AuthenticationPrincipal UserDetailsImpl principal,
                @RequestParam(defaultValue = "0") Integer page,
                @RequestParam(defaultValue = "0") Integer size,
                @RequestParam(required = false) String sort,
                @RequestParam(defaultValue = "ASC") String type) {

            Long userId = principal.getId();
            Page<TicketResponse> body = ticketService.getPassedTickets(userId, page, size, sort, type);

            ResponseMessage<Page<TicketResponse>> response = ResponseMessage.<Page<TicketResponse>>builder()
                    .returnBody(body)
                    .message(SuccessMessages.PASSED_TICKETS_LISTED)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return ResponseEntity.ok(response);
        }

        @PreAuthorize("permitAll()")
        @PostMapping("/reserve")
        public ResponseEntity<ResponseMessage<List<TicketResponse>>> reserve(
                @AuthenticationPrincipal com.cinetime.security.service.UserDetailsImpl principal,
                @RequestBody ReserveTicketRequest req) {
            Long userId = principal != null ? principal.getId() : null; // allow guest if null
            List<TicketResponse> body = ticketService.reserve(req, userId);

            ResponseMessage<List<TicketResponse>> response = ResponseMessage.<List<TicketResponse>>builder()
                    .returnBody(body)
                    .message(SuccessMessages.TICKET_RESERVED)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return ResponseEntity.ok(response);
        }
        @PreAuthorize("permitAll()")
        @PostMapping("/buy-ticket")
        public ResponseEntity<ResponseMessage<List<TicketResponse>>> buy(
                @AuthenticationPrincipal com.cinetime.security.service.UserDetailsImpl principal,
                @RequestBody @Valid BuyTicketRequest request) {
            Long userId = principal != null ? principal.getId() : null;
            List<TicketResponse> body = ticketService.buy(request, userId);

            ResponseMessage<List<TicketResponse>> response = ResponseMessage.<List<TicketResponse>>builder()
                    .returnBody(body)
                    .message(SuccessMessages.TICKET_BOUGHT)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return new ResponseEntity<>(response, org.springframework.http.HttpStatus.CREATED);
        }
    }
