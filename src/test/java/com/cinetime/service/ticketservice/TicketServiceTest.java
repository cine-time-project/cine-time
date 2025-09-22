package com.cinetime.service.business;

import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.business.Ticket;
import com.cinetime.entity.enums.TicketStatus;
import com.cinetime.entity.user.User;
import com.cinetime.payload.mappers.TicketMapper;
import com.cinetime.payload.request.business.BuyTicketRequest;
import com.cinetime.payload.request.business.ReserveTicketRequest;
import com.cinetime.payload.response.business.TicketResponse;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.business.TicketRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.business.TicketService;
import com.cinetime.service.helper.PageableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    /**
     * Mirrors the style of HallServiceTest: Mockito + JUnit5, no Spring context.
     */

        @Mock
        TicketRepository ticketRepository;
        @Mock
        ShowtimeRepository showtimeRepository;
        @Mock
        UserRepository userRepository;
        @Mock
        TicketMapper ticketMapper;
        @Mock
        PageableHelper pageableHelper;

        @InjectMocks
        TicketService ticketService;

        private User user;
        private Showtime showtimeFuture;
        private Showtime showtimePast;

        @BeforeEach
        void setUp() {
            user = User.builder().id(1L).email("member@cinetime.local").build();

            var today = LocalDate.now();
            var now   = LocalTime.now();

            showtimeFuture = new Showtime();
            showtimeFuture.setId(10L);
            showtimeFuture.setDate(today.plusDays(1));
            showtimeFuture.setStartTime(now);
            showtimeFuture.setEndTime(now.plusHours(2));

            showtimePast = new Showtime();
            showtimePast.setId(11L);
            showtimePast.setDate(today.minusDays(1));
            showtimePast.setStartTime(LocalTime.of(18, 0));
            showtimePast.setEndTime(LocalTime.of(20, 0));
        }

        // ---------- BUY ----------

        @Test
        void buy_shouldCreateTickets_whenSeatsAvailable_andShowtimeIsFuture() {
            var req = new BuyTicketRequest();
            req.setMovieName("Fight Club");
            req.setCinema("CineTime Downtown");
            req.setHall("Hall 1");
            req.setDate(showtimeFuture.getDate());
            req.setShowtime(showtimeFuture.getStartTime());
            req.setSeatInformation(List.of(
                     BuyTicketRequest.SeatInfo.builder()
                            .seatLetter("B").
                            seatNumber(12)
                            .build(),
                    BuyTicketRequest.SeatInfo.builder()
                            .seatLetter("B")
                            .seatNumber(13).build()));

            when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                    eq("Fight Club"), eq("Hall 1"), eq("CineTime Downtown"),
                    eq(req.getDate()), eq(req.getShowtime())
            )).thenReturn(Optional.of(showtimeFuture));

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // seats not taken
            when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(10L, "B", 12)).thenReturn(false);
            when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(10L, "B", 13)).thenReturn(false);

            // persist two tickets
            var t1 = Ticket.builder().id(100L).showtime(showtimeFuture).user(user).seatLetter("B").seatNumber(12).status(TicketStatus.PAID).build();
            var t2 = Ticket.builder().id(101L).showtime(showtimeFuture).user(user).seatLetter("B").seatNumber(13).status(TicketStatus.PAID).build();
            when(ticketRepository.saveAll(anyList())).thenReturn(List.of(t1, t2));

            // map
            var r1 = mock(TicketResponse.class);
            var r2 = mock(TicketResponse.class);
            when(ticketMapper.mapTicketToTicketResponse(t1)).thenReturn(r1);
            when(ticketMapper.mapTicketToTicketResponse(t2)).thenReturn(r2);

            var result = ticketService.buy(req, 1L);

            assertThat(result).containsExactly(r1, r2);
            verify(ticketRepository, times(1))
                    .saveAll(argThat((Iterable<Ticket> it) -> {
                        int count = 0;
                        for (Ticket ignored : it) count++;
                        return count == 2;
                    }));
        }

        @Test
        void buy_shouldThrow409StyleException_whenSeatAlreadyTaken() {
            var req = new BuyTicketRequest();
            req.setMovieName("Fight Club");
            req.setCinema("CineTime Downtown");
            req.setHall("Hall 1");
            req.setDate(showtimeFuture.getDate());
            req.setShowtime(showtimeFuture.getStartTime());
           // req.setSeatInformation(List.of(new BuyTicketRequest.SeatDto("Z", 6)));
            req.setSeatInformation(List.of(new BuyTicketRequest.SeatInfo("Z", 6)));

            when(showtimeRepository.findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
                    anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalTime.class)
            )).thenReturn(Optional.of(showtimeFuture));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(ticketRepository.existsByShowtime_IdAndSeatLetterAndSeatNumber(10L, "Z", 6)).thenReturn(true);

            assertThatThrownBy(() -> ticketService.buy(req, 1L))
                    .isInstanceOf(com.cinetime.exception.ConflictException.class) // replace with your SeatAlreadyTakenException if you added it
                    .hasMessageContaining("already");

            verify(ticketRepository, never()).saveAll(anyList());
        }

        // ---------- RESERVE ----------

        @Test
        void reserve_shouldCreateReservedTickets_whenFutureShowtime() {
            var req = new ReserveTicketRequest();
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

            var saved = Ticket.builder().id(200L).showtime(showtimeFuture).user(user).seatLetter("A").seatNumber(1).status(TicketStatus.RESERVED).build();
            when(ticketRepository.saveAll(anyList())).thenReturn(List.of(saved));
            var resp = mock(TicketResponse.class);
            when(ticketMapper.mapTicketToTicketResponse(saved)).thenReturn(resp);

            var result = ticketService.reserve(req, 1L);
            assertThat(result).containsExactly(resp);
        }

        @Test
        void reserve_shouldReject_whenShowtimeInPast() {
            var req = new ReserveTicketRequest();
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

        // ---------- LISTING ----------

        @Test
        void getCurrentTickets_shouldReturnMappedPage() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
            when(pageableHelper.buildPageable(0, 10, null, "ASC")).thenReturn(pageable);

            var t = Ticket.builder().id(1L).showtime(showtimeFuture).user(user).seatLetter("B").seatNumber(12).status(TicketStatus.PAID).build();
            var page = new PageImpl<>(List.of(t), pageable, 1);
            when(ticketRepository.findCurrentForUser(eq(1L), anyList(), any(LocalDate.class), any(LocalTime.class), eq(pageable)))
                    .thenReturn(page);

            var r = mock(TicketResponse.class);
            when(ticketMapper.mapTicketToTicketResponse(t)).thenReturn(r);

            var result = ticketService.getCurrentTickets(1L, 0, 10, null, "ASC");
            assertThat(result.getContent()).containsExactly(r);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void getPassedTickets_shouldReturnMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(pageableHelper.buildPageable(0, 10, null, "ASC")).thenReturn(pageable);

            var t = Ticket.builder().id(2L).showtime(showtimePast).user(user).seatLetter("C").seatNumber(3).status(TicketStatus.PAID).build();
            var page = new PageImpl<>(List.of(t), pageable, 1);
            when(ticketRepository.findPassedForUserAt(eq(1L), eq(TicketStatus.USED), eq(TicketStatus.PAID),
                    any(LocalDate.class), any(LocalTime.class), eq(pageable)))
                    .thenReturn(page);

            var r = mock(TicketResponse.class);
            when(ticketMapper.mapTicketToTicketResponse(t)).thenReturn(r);

            var result = ticketService.getPassedTickets(1L, 0, 10, null, "ASC");
            assertThat(result.getContent()).containsExactly(r);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }
