package com.cinetime.service.business;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.ShowtimeMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.*;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.ShowtimeRepository;
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

    public Showtime findShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.SHOWTIME_NOT_FOUND_ID, id)));
    }

    public ResponseMessage<ShowtimeResponse> deleteShowtimeById(Long id) {
        Showtime showtime = findShowtimeById(id);
        ShowtimeResponse response = showtimeMapper.mapShowtimeToResponse(showtime);
        showtimeRepository.delete(showtime);
        return ResponseMessage.<ShowtimeResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIME_DELETED)
                .returnBody(response)
                .build();
    }


    public ResponseMessage<ShowtimeResponse> getShowtimeById(Long id) {
        Showtime showtime = findShowtimeById(id);
        return ResponseMessage.<ShowtimeResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIME_FOUND)
                .returnBody(showtimeMapper.mapShowtimeToResponse(showtime))
                .build();
    }

    @Transactional
    public ResponseMessage<ShowtimeResponse> updateShowtimeById(Long id, ShowtimeRequest showtimeRequest) {
        Showtime showtime = findShowtimeById(id);

        Hall hall = (showtimeRequest.getHallId() != null) ? hallService.findHallById(showtimeRequest.getHallId()) : null;
        Movie movie = (showtimeRequest.getMovieId() != null) ? movieService.findMovieById(showtimeRequest.getMovieId()) : null;

        showtimeMapper.updateShowtimeFromRequest(showtime, showtimeRequest, hall, movie);

        Showtime updatedShowtime = showtimeRepository.save(showtime);

        return ResponseMessage.<ShowtimeResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SHOWTIME_UPDATED)
                .returnBody(showtimeMapper.mapShowtimeToResponse(updatedShowtime))
                .build();
    }

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


}
