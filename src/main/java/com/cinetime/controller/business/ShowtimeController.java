package com.cinetime.controller.business;

import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.*;
import com.cinetime.service.business.ShowtimeService;
import com.cinetime.service.business.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/show-times") // <--Changed for multi base path
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showTimeService;
    private final TicketService ticketService;

    @GetMapping("/slots")
    public List<ShowtimeResponse> getSlots(
            @RequestParam Long cinemaId,
            @RequestParam Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return showTimeService.getSlotsByCinemaMovieDate(cinemaId, movieId, date);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<ShowtimeResponse> saveShowtime(
            @RequestBody @Valid ShowtimeRequest showtimeRequest) {
        return showTimeService.saveShowtime(showtimeRequest);
    }

    // ShowtimesController.java
    @GetMapping("/unavailable-seats")
    @PreAuthorize("permitAll()")
    public List<String> unavailableSeatsByFields(
            @RequestParam String movieName,
            @RequestParam String hall,
            @RequestParam String cinema,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime showtime
    ) {
        return ticketService.getTakenSeatsByFields(movieName, hall, cinema, date, showtime);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<ShowtimeResponse> deleteShowtimeById(@PathVariable Long id) {
        return showTimeService.deleteShowtimeById(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseMessage<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        return showTimeService.getShowtimeById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Transactional
    public ResponseMessage<ShowtimeResponse> updateShowtimeById(
            @PathVariable Long id,
            @RequestBody @Valid ShowtimeRequest showtimeRequest) {
        return showTimeService.updateShowtimeById(id, showtimeRequest);
    }

    @GetMapping("/movie/{movieId}")
    @PreAuthorize("permitAll()")
    public ResponseMessage<Page<ShowtimeResponse>> getShowtimesByMovieId(
            @PathVariable Long movieId,
            @PageableDefault(page = 0, size = 10, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        return showTimeService.getShowtimesByMovieId(movieId, pageable);
    }

    // S01 Endpoint - Get showtimes by cinema ID
    @GetMapping("/cinema/{cinemaId}")
    @PreAuthorize("permitAll()")
    public ResponseMessage<List<HallWithShowtimesResponse>> getShowtimesByCinemaId(@PathVariable Long cinemaId) {
        return showTimeService.getShowtimesByCinemaId(cinemaId);
    }

    @GetMapping("/cities-with-showtimes")
    @PreAuthorize("permitAll()")
    public ResponseMessage<List<CityMiniResponse>> getCitiesWithShowtimes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onOrAfter,
            @RequestParam(required = false) Long movieId
    ) {
        return showTimeService.getCitiesWithShowtimes(onOrAfter, movieId);
    }





    @GetMapping("/countries-with-showtimes")
    @PreAuthorize("permitAll()")
    public ResponseMessage<List<CountryMiniResponse>> getCountriesWithShowtimes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onOrAfter,
            @RequestParam(required = false) Long movieId
    ) {
        return showTimeService.getCountriesWithShowtimes(onOrAfter, movieId);
    }


}
