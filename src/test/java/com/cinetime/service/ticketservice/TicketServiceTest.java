package com.cinetime.service.ticketservice;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Payment;
import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.PaymentStatus;
import com.cinetime.entity.enums.TicketStatus;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.PaymentMapper;
import com.cinetime.payload.mappers.TicketMapper;
import com.cinetime.payload.request.business.BuyTicketRequest;
import com.cinetime.payload.request.business.ReserveTicketRequest;
import com.cinetime.payload.response.business.PaymentResponse;
import com.cinetime.payload.response.business.TicketResponse;
import com.cinetime.repository.business.PaymentRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.business.TicketRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.business.TicketService;
import com.cinetime.service.helper.MailHelper;
import com.cinetime.service.helper.PageableHelper;
import com.cinetime.service.mail.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock TicketRepository ticketRepository;
    @Mock ShowtimeRepository showtimeRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock TicketMapper ticketMapper;
    @Mock PaymentMapper paymentMapper;
    @Mock PageableHelper pageableHelper;
    @Mock MailService mailService;
    @Mock MailHelper mailHelper;

    @InjectMocks
    TicketService ticketService;

    private static final String IDEMPOTENCY_KEY = "test-idem-key";

    private User user;
    private Showtime showtimeFuture;
    private Showtime showtimePast;
    private Hall hall;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("member@cinetime.local")
                .name("Member")
                .surname("One")
                .build();

        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        // ✅ Create Hall entity
        hall = Hall.builder()
                .id(1L)
                .name("Main Hall")
                .seatCapacity(100)
                .isSpecial(false)
                .build();

        showtimeFuture = new Showtime();
        showtimeFuture.setId(10L);
        showtimeFuture.setDate(today.plusDays(1));
        showtimeFuture.setStartTime(now);
        showtimeFuture.setEndTime(now.plusHours(2));
        showtimeFuture.setHall(hall);  // ✅ SET HALL

        showtimePast = new Showtime();
        showtimePast.setId(11L);
        showtimePast.setDate(today.minusDays(1));
        showtimePast.setStartTime(LocalTime.of(18, 0));
        showtimePast.setEndTime(LocalTime.of(20, 0));
        showtimePast.setHall(hall);  // ✅ SET HALL
    }

    private BuyTicketRequest.SeatInfo seat(String letter, int number) {
        BuyTicketRequest.SeatInfo s = new BuyTicketRequest.SeatInfo();
        s.setSeatLetter(letter);
        s.setSeatNumber(number);
        return s;
    }

    private BuyTicketRequest buyReq(String movie, String hall, String cinema, LocalDate date, LocalTime time, List<BuyTicketRequest.SeatInfo> seats) {
        BuyTicketRequest req = new BuyTicketRequest();
        req.setMovieName(movie);
        req.setHall(hall);
        req.setCinema(cinema);
        req.setDate(date);
        req.setShowtime(time);
        req.setSeatInformation(seats);
        return req;
    }

    private Ticket makeTicket(String letter, int num, Showtime st, User u, Payment p) {
        Ticket t = new Ticket();
        t.setSeatLetter(letter);
        t.setSeatNumber(num);
        t.setStatus(TicketStatus.PAID);
        t.setPrice(9.99);
        t.setShowtime(st);
        t.setUser(u);
        t.setPayment(p);
        return t;
    }

    // ---------------- BUY ----------------

    @Test
    void buy_success_multipleSeats_returnsPaymentResponse_andSendsEmail() {
        BuyTicketRequest req = buyReq(
                "Fight Club", "Hall 1", "CineTime Downtown",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(),
                List.of(seat("B",12), seat("B",13))
        );

        // No existing idempotent payment
        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());

        // Resolve showtime + user
        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                eq("Fight Club"), eq("Hall 1"), eq("CineTime Downtown"),
                eq(req.getDate()), eq(req.getShowtime())
        )).thenReturn(Optional.of(showtimeFuture));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Create PENDING payment with id
        Payment pending = Payment.builder()
                .id(77L)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .idempotencyKey(IDEMPOTENCY_KEY)
                .currency("USD")
                .amount(0.0)
                .user(user)
                .build();
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(pending);

        // ✅ Mock seat capacity check
        when(ticketRepository.countByShowtime_IdAndStatusIn(
                eq(10L),
                eq(List.of(TicketStatus.PAID, TicketStatus.RESERVED))
        )).thenReturn(5L);  // 5 seats taken, 95 available

        // Seats free
        when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(10L, "B", 12)).thenReturn(false);
        when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(10L, "B", 13)).thenReturn(false);

        // Persist tickets
        Ticket t1 = makeTicket("B",12, showtimeFuture, user, pending); t1.setId(100L);
        Ticket t2 = makeTicket("B",13, showtimeFuture, user, pending); t2.setId(101L);
        List<Ticket> persisted = List.of(t1, t2);
        when(ticketRepository.saveAllAndFlush(anyList())).thenReturn(persisted);

        // Finalize payment
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mapper result
        PaymentResponse mapped = PaymentResponse.builder()
                .tickets(List.of(
                        TicketResponse.builder().id(100L).build(),
                        TicketResponse.builder().id(101L).build()
                ))
                .paymentId(77L)
                .paymentAmount(19.98)
                .paymentCurrency("USD")
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        when(paymentMapper.mapPaymentToPaymentResponse(any(Payment.class), eq(persisted))).thenReturn(mapped);

        // Act
        PaymentResponse response = ticketService.buy(req, 1L, IDEMPOTENCY_KEY);

        // Assert
        assertThat(response.getPaymentId()).isEqualTo(77L);
        assertThat(response.getTickets()).hasSize(2);
        verify(ticketRepository, times(1)).saveAllAndFlush(anyList());
        verify(mailHelper, times(1)).sendPurchaseReceipt(eq("member@cinetime.local"), any(Payment.class), eq(persisted));
    }

    @Test
    void buy_idempotent_existingPayment_returnsSameResponse_noNewWrites() {
        Payment existing = new Payment();
        existing.setId(99L);
        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.of(existing));

        List<Ticket> existingTickets = List.of(new Ticket(), new Ticket(), new Ticket());
        when(ticketRepository.findAllByPaymentId(99L)).thenReturn(existingTickets);

        PaymentResponse mapped = PaymentResponse.builder()
                .paymentId(99L)
                .tickets(List.of(new TicketResponse(), new TicketResponse(), new TicketResponse()))
                .build();
        when(paymentMapper.mapPaymentToPaymentResponse(existing, existingTickets)).thenReturn(mapped);

        BuyTicketRequest req = buyReq("Anything","H","C",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(),
                List.of(seat("A",1)));

        PaymentResponse result = ticketService.buy(req, 1L, IDEMPOTENCY_KEY);

        assertThat(result.getPaymentId()).isEqualTo(99L);
        verify(showtimeRepository, never()).findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(any(),any(),any(),any(),any());
        verify(ticketRepository, never()).saveAllAndFlush(anyList());
        verify(mailHelper, never()).sendPurchaseReceipt(anyString(), any(Payment.class), anyList());
    }

    @Test
    void buy_showtimeNotFound_throwsResourceNotFound() {
        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.empty());

        BuyTicketRequest req = buyReq("Fight Club","Hall 1","CineTime Downtown",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(), List.of(seat("B",12)));

        assertThatThrownBy(() -> ticketService.buy(req, 1L, IDEMPOTENCY_KEY))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Showtime not found");
    }

    @Test
    void buy_userNotFound_throwsResourceNotFound() {
        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.of(showtimeFuture));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BuyTicketRequest req = buyReq("Fight Club","Hall 1","CineTime Downtown",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(), List.of(seat("B",12)));

        assertThatThrownBy(() -> ticketService.buy(req, 1L, IDEMPOTENCY_KEY))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void buy_emptySeatList_throwsIllegalArgument() {
        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.of(showtimeFuture));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(new Payment());

        BuyTicketRequest req = buyReq("Fight Club","Hall 1","CineTime Downtown",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(), Collections.emptyList());

        assertThatThrownBy(() -> ticketService.buy(req, 1L, IDEMPOTENCY_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("seatInformation must be a non-empty array");
        verify(ticketRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void buy_seatAlreadyTaken_conflict() {
        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.of(showtimeFuture));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(new Payment());

        // ✅ Mock seat capacity check
        when(ticketRepository.countByShowtime_IdAndStatusIn(
                eq(10L),
                eq(List.of(TicketStatus.PAID, TicketStatus.RESERVED))
        )).thenReturn(5L);

        when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(eq(10L), eq("Z"), eq(6))).thenReturn(true);

        BuyTicketRequest req = buyReq("Fight Club","Hall 1","CineTime Downtown",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(), List.of(seat("Z",6)));

        assertThatThrownBy(() -> ticketService.buy(req, 1L, IDEMPOTENCY_KEY))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already");
        verify(ticketRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void buy_dbDuplicate_translatesToConflict() {
        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.of(showtimeFuture));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(new Payment());

        // ✅ Mock seat capacity check
        when(ticketRepository.countByShowtime_IdAndStatusIn(
                eq(10L),
                eq(List.of(TicketStatus.PAID, TicketStatus.RESERVED))
        )).thenReturn(5L);

        when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(anyLong(), anyString(), anyInt()))
                .thenReturn(false);
        when(ticketRepository.saveAllAndFlush(anyList())).thenThrow(new DataIntegrityViolationException("dup"));

        BuyTicketRequest req = buyReq("Fight Club","Hall 1","CineTime Downtown",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(), List.of(seat("A",1)));

        assertThatThrownBy(() -> ticketService.buy(req, 1L, IDEMPOTENCY_KEY))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("reserved/paid");
    }

    @Test
    void buy_emailFails_purchaseStillSucceeds() {
        BuyTicketRequest req = buyReq("Fight Club","Hall 1","CineTime Downtown",
                showtimeFuture.getDate(), showtimeFuture.getStartTime(), List.of(seat("A",1)));

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.of(showtimeFuture));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Payment pending = new Payment(); pending.setId(500L);
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(pending);

        // ✅ Mock seat capacity check
        when(ticketRepository.countByShowtime_IdAndStatusIn(
                eq(10L),
                eq(List.of(TicketStatus.PAID, TicketStatus.RESERVED))
        )).thenReturn(5L);

        when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(anyLong(), anyString(), anyInt()))
                .thenReturn(false);

        Ticket saved = makeTicket("A",1, showtimeFuture, user, pending); saved.setId(700L);
        when(ticketRepository.saveAllAndFlush(anyList())).thenReturn(List.of(saved));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // simulate mail failure thrown inside helper
        doThrow(new RuntimeException("smtp down"))
                .when(mailHelper).sendPurchaseReceipt(eq("member@cinetime.local"), any(Payment.class), anyList());

        PaymentResponse mapped = PaymentResponse.builder()
                .paymentId(500L)
                .tickets(List.of(new TicketResponse()))
                .build();
        when(paymentMapper.mapPaymentToPaymentResponse(any(Payment.class), anyList()))
                .thenReturn(mapped);

        PaymentResponse resp = ticketService.buy(req, 1L, IDEMPOTENCY_KEY);

        assertThat(resp.getPaymentId()).isEqualTo(500L);
        verify(mailHelper, times(1)).sendPurchaseReceipt(anyString(), any(Payment.class), anyList());
    }

    // ---------------- RESERVE ----------------

    @Test
    void reserve_shouldCreateReservedTickets_whenFutureShowtime() {
        ReserveTicketRequest req = new ReserveTicketRequest();
        req.setMovieName("Fight Club");
        req.setCinema("CineTime Downtown");
        req.setHall("Hall 1");
        req.setDate(showtimeFuture.getDate());
        req.setShowtime(showtimeFuture.getStartTime());
        req.setSeatInformation(List.of(
                BuyTicketRequest.SeatInfo.builder().seatLetter("A").seatNumber(1).build()
        ));

        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.of(showtimeFuture));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(10L, "A", 1)).thenReturn(false);

        Ticket saved = new Ticket();
        saved.setId(200L);
        saved.setShowtime(showtimeFuture);
        saved.setUser(user);
        saved.setSeatLetter("A");
        saved.setSeatNumber(1);
        saved.setStatus(TicketStatus.RESERVED);
        when(ticketRepository.saveAll(anyList())).thenReturn(List.of(saved));

        TicketResponse resp = TicketResponse.builder().id(200L).build();
        when(ticketMapper.mapTicketToTicketResponse(saved)).thenReturn(resp);

        List<TicketResponse> result = ticketService.reserve(req, 1L);
        assertThat(result).containsExactly(resp);
    }

    @Test
    void reserve_shouldReject_whenShowtimeInPast() {
        ReserveTicketRequest req = new ReserveTicketRequest();
        req.setMovieName("Fight Club");
        req.setCinema("CineTime Downtown");
        req.setHall("Hall 1");
        req.setDate(showtimePast.getDate());
        req.setShowtime(showtimePast.getStartTime());
        req.setSeatInformation(List.of(
                BuyTicketRequest.SeatInfo.builder()
                        .seatLetter("G")
                        .seatNumber(4)
                        .build()
        ));

        when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(Optional.of(showtimePast));

        assertThatThrownBy(() -> ticketService.reserve(req, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("upcoming");

        verify(ticketRepository, never()).saveAll(anyList());
    }

    // ---------------- LISTING ----------------

    @Test
    void getCurrentTickets_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(pageableHelper.buildPageable(0, 10, null, "ASC")).thenReturn(pageable);

        Ticket t = new Ticket();
        t.setId(1L);
        t.setShowtime(showtimeFuture);
        t.setUser(user);
        t.setSeatLetter("B");
        t.setSeatNumber(12);
        t.setStatus(TicketStatus.PAID);

        PageImpl<Ticket> page = new PageImpl<>(List.of(t), pageable, 1);
        when(ticketRepository.findCurrentForUser(eq(1L), anyList(), any(LocalDate.class), any(LocalTime.class), eq(pageable)))
                .thenReturn(page);

        TicketResponse r = TicketResponse.builder().id(1L).build();
        when(ticketMapper.mapTicketToTicketResponse(t)).thenReturn(r);

        org.springframework.data.domain.Page<TicketResponse> result = ticketService.getCurrentTickets(1L, 0, 10, null, "ASC");
        assertThat(result.getContent()).containsExactly(r);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getPassedTickets_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pageableHelper.buildPageable(0, 10, null, "ASC")).thenReturn(pageable);

        Ticket t = new Ticket();
        t.setId(2L);
        t.setShowtime(showtimePast);
        t.setUser(user);
        t.setSeatLetter("C");
        t.setSeatNumber(3);
        t.setStatus(TicketStatus.PAID);

        PageImpl<Ticket> page = new PageImpl<>(List.of(t), pageable, 1);
        when(ticketRepository.findPassedForUserAt(eq(1L), eq(TicketStatus.USED), eq(TicketStatus.PAID),
                any(LocalDate.class), any(LocalTime.class), eq(pageable)))
                .thenReturn(page);

        TicketResponse r = TicketResponse.builder().id(2L).build();
        when(ticketMapper.mapTicketToTicketResponse(t)).thenReturn(r);

        org.springframework.data.domain.Page<TicketResponse> result = ticketService.getPassedTickets(1L, 0, 10, null, "ASC");
        assertThat(result.getContent()).containsExactly(r);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}