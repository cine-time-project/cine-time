package com.cinetime.service.business;

import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CityMapper;
import com.cinetime.payload.mappers.ShowtimeMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.*;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.business.TicketRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final HallService hallService;
    private final MovieService movieService;
    private final ShowtimeMapper showtimeMapper;
    private final CinemaRepository cinemaRepository;
    private final CityMapper cityMapper;
    private final TicketRepository ticketRepository;

    @Transactional
    public ResponseMessage<ShowtimeResponse> saveShowtime(@Valid ShowtimeRequest showtimeRequest) {
        Hall hall = hallService.findHallById(showtimeRequest.getHallId());
        Movie movie = movieService.findMovieById(showtimeRequest.getMovieId());
        Showtime showtime = showtimeMapper.mapRequestToShowtime(showtimeRequest, hall, movie);
        Showtime savedShowtime = showtimeRepository.save(showtime);
        return ResponseMessage.<ShowtimeResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message(SuccessMessages.SHOWTIME_CREATED)
                .returnBody(showtimeMapper.mapShowtimeToResponse(savedShowtime))
                .build();
    }

    public ResponseMessage<List<CityMiniResponse>> getCitiesWithShowtimes(LocalDate onOrAfter, Long movieId) {
        List<CityMiniResponse> body = showtimeRepository.findCitiesWithShowtimes(onOrAfter, movieId);
        return ResponseMessage.<List<CityMiniResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Cities with showtimes")
                .returnBody(body)
                .build();
    }


    @Transactional(readOnly = true)
    public Showtime findShowtimeById(Long id) {
        // EntityGraph veya Query’li olanı kullanabilirsin
        return showtimeRepository.findWithRefsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.SHOWTIME_NOT_FOUND_ID, id)));
        //        .orElseThrow(() -> new EntityNotFoundException("Showtime not found: " + id));
    }


    @Transactional
    public ResponseMessage<ShowtimeResponse> deleteShowtimeById(Long id) {
        Showtime s = findShowtimeById(id);
        ShowtimeResponse body = showtimeMapper.mapShowtimeToResponse(s);

        showtimeRepository.delete(s);

        return ResponseMessage.<ShowtimeResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIME_DELETED)
                .returnBody(body)
                .build();
    }


    @Transactional(readOnly = true)
    public ResponseMessage<ShowtimeResponse> getShowtimeById(Long id) {
        Showtime s = findShowtimeById(id);
        ShowtimeResponse body = showtimeMapper.mapShowtimeToResponse(s);
        return ResponseMessage.<ShowtimeResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIME_FOUND)
                .returnBody(body)
                .build();
    }

    @Transactional
    public ResponseMessage<ShowtimeResponse> updateShowtimeById(Long id, ShowtimeRequest req) {
        Showtime s = findShowtimeById(id); // ilişkileri açıkken geldi
        Hall hall   = (req.getHallId()  != null) ? hallService.findHallById(req.getHallId())   : null;
        Movie movie = (req.getMovieId() != null) ? movieService.findMovieById(req.getMovieId()): null;

        showtimeMapper.updateShowtimeFromRequest(s, req, hall, movie);
        Showtime saved = showtimeRepository.save(s);

        return ResponseMessage.<ShowtimeResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIME_UPDATED)
                .returnBody(showtimeMapper.mapShowtimeToResponse(saved))
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseMessage<Page<ShowtimeResponse>> getShowtimesByMovieId(Long movieId, Pageable pageable) {
        Movie movie = movieService.findMovieById(movieId);
        Page<Showtime> showtimes = showtimeRepository.findAllByMovie(movie, pageable);
        if (showtimes.isEmpty()) throw new ResourceNotFoundException(ErrorMessages.SHOWTIMES_NOT_FOUND);
        return ResponseMessage.<Page<ShowtimeResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIMES_FOUND)
                .returnBody(showtimeMapper.mapToResponsePage(showtimes))
                .build();
    }

    // S01 Endpoint - Get showtimes by cinema ID
    public ResponseMessage<List<HallWithShowtimesResponse>> getShowtimesByCinemaId(Long cinemaId) {
        // Validate cinema exists using repository directly
        cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.CINEMA_NOT_FOUND, cinemaId)));

        List<ShowtimeRepository.HallMovieTimeRow> rows = showtimeRepository.findShowtimesByCinemaId(cinemaId);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException(ErrorMessages.SHOWTIMES_NOT_FOUND);
        }

        List<HallWithShowtimesResponse> hallResponses = groupShowtimesByHallAndMovie(rows);

        return ResponseMessage.<List<HallWithShowtimesResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIMES_FOUND_BY_CINEMA)
                .returnBody(hallResponses)
                .build();
    }

    private List<HallWithShowtimesResponse> groupShowtimesByHallAndMovie(List<ShowtimeRepository.HallMovieTimeRow> rows) {
        // Group by hall
        Map<Long, List<ShowtimeRepository.HallMovieTimeRow>> hallGroups = rows.stream()
                .collect(Collectors.groupingBy(ShowtimeRepository.HallMovieTimeRow::getHallId));

        return hallGroups.entrySet().stream()
                .map(hallEntry -> {
                    Long hallId = hallEntry.getKey();
                    List<ShowtimeRepository.HallMovieTimeRow> hallRows = hallEntry.getValue();

                    // Get hall info from first row
                    ShowtimeRepository.HallMovieTimeRow firstRow = hallRows.get(0);

                    // Group by movie within this hall
                    Map<Long, List<ShowtimeRepository.HallMovieTimeRow>> movieGroups = hallRows.stream()
                            .collect(Collectors.groupingBy(ShowtimeRepository.HallMovieTimeRow::getMovieId));

                    List<HallMovieShowtimesResponse> movieShowtimes = movieGroups.entrySet().stream()
                            .map(movieEntry -> {
                                List<ShowtimeRepository.HallMovieTimeRow> movieRows = movieEntry.getValue();
                                ShowtimeRepository.HallMovieTimeRow movieFirstRow = movieRows.get(0);

                                MovieMiniResponse movie = MovieMiniResponse.builder()
                                        .id(movieFirstRow.getMovieId())
                                        .title(movieFirstRow.getMovieTitle())
                                        .build();

                                List<LocalDateTime> times = movieRows.stream()
                                        .map(row -> LocalDateTime.of(row.getDate(), row.getStartTime()))
                                        .sorted()
                                        .collect(Collectors.toList());

                                return HallMovieShowtimesResponse.builder()
                                        .movie(movie)
                                        .times(times)
                                        .build();
                            })
                            .sorted(Comparator.comparing(hms -> hms.getMovie().getTitle()))
                            .collect(Collectors.toList());

                    return HallWithShowtimesResponse.builder()
                            .id(hallId)
                            .name(firstRow.getHallName())
                            .seatCapacity(firstRow.getSeatCapacity())
                            .isSpecial(firstRow.getIsSpecial())
                            .movies(movieShowtimes)
                            .build();
                })
                .sorted(Comparator.comparing(HallWithShowtimesResponse::getName))
                .collect(Collectors.toList());
    }

    public ResponseMessage<List<CountryMiniResponse>> getCountriesWithShowtimes(LocalDate onOrAfter, Long movieId) {
        List<CountryMiniResponse> body = showtimeRepository.findCountriesWithShowtimes(onOrAfter, movieId);
        return ResponseMessage.<List<CountryMiniResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Countries with showtimes")
                .returnBody(body)
                .build();
    }


    // BUNU TUT (entity -> mapper)
    public ResponseMessage<List<CityMiniResponse>> getCitiesWithShowtimesByCountry(
            LocalDate onOrAfter, Long movieId, Long countryId) {

        List<City> cities = showtimeRepository
                .findCitiesWithShowtimesByCountryTic(onOrAfter, movieId, countryId);

        List<CityMiniResponse> body = cities.stream()
                .map(cityMapper::cityToCityMiniResponse)
                .toList();

        return ResponseMessage.<List<CityMiniResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Cities fetched")
                .returnBody(body)
                .build();
    }


    @Transactional(readOnly = true)
    public ResponseMessage<Page<ShowtimeResponse>> getAllShowtimes(
            Pageable pageable,
            Long cinemaId,
            Long hallId,
            Long movieId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        Page<Showtime> page = showtimeRepository.findAllFiltered(
                cinemaId, hallId, movieId, dateFrom, dateTo, pageable
        );

        Page<ShowtimeResponse> dtoPage = page.map(showtimeMapper::mapShowtimeToResponse);

        return ResponseMessage.<Page<ShowtimeResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Showtimes listed successfully.")
                .returnBody(dtoPage)
                .build();
    }
}
