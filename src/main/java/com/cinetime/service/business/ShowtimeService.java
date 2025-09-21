package com.cinetime.service.business;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.payload.mappers.ShowtimeMapper;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.business.ShowtimeResponse;
import com.cinetime.repository.business.ShowtimeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final HallService hallService;
    private final MovieService movieService;
    private final ShowtimeMapper showtimeMapper;

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
}
