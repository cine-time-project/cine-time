package com.cinetime.service.helper;


import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class MovieServiceHelper {

    private final ShowtimeRepository showtimeRepository;
    private final MovieMapper movieMapper;
    private final MovieRepository movieRepository;


    public ResponseMessage<Page<MovieResponse>> getMoviesByDate(LocalDate date, Pageable pageable) {
        Page<Showtime> showtimes;
        if (date.isBefore(LocalDate.now())) {
            showtimes = showtimeRepository.findByDateAndStartTimeAfter(LocalDate.now(), LocalTime.now(), pageable);
        } else {
            showtimes = showtimeRepository.findByDate(date, pageable);
        }
        if (showtimes.isEmpty()) {
            return createResponseMessage(Page.empty(pageable),
                    ErrorMessages.MOVIES_NOT_FOUND_ON_DATE + " " + date, HttpStatus.OK);
        }
        Page<MovieResponse> movies = showtimes.map(s -> movieMapper.mapMovieToMovieResponse(s.getMovie()));
        return createResponseMessage(
                movies,
                SuccessMessages.MOVIES_FOUND_ON_DATE + " " + date,
                HttpStatus.OK);
    }

    public ResponseMessage<Page<MovieResponse>> createResponseMessage(Page<MovieResponse> body, String message, HttpStatus status) {
        return ResponseMessage.<Page<MovieResponse>>builder()
                .returnBody(body)
                .message(message)
                .httpStatus(status)
                .build();
    }

    public ResponseMessage<Page<MovieResponse>> getCurrentlyInTheatres(Pageable pageable) {
        Page<Movie> movies = movieRepository.findByStatus(MovieStatus.IN_THEATERS, pageable);

        if (movies.isEmpty()) {
            return createResponseMessage(
                    Page.empty(pageable),
                    ErrorMessages.MOVIES_NOT_IN_THEATRES,
                    HttpStatus.OK);
        }
        return createResponseMessage(
                movieMapper.mapToResponsePage(movies),
                SuccessMessages.MOVIES_FOUND_IN_THEATRES,
                HttpStatus.OK);
    }

    public ResponseMessage<Page<MovieResponse>> getComingSoonByDate(LocalDate date, Pageable pageable) {
        Page<Movie> movies;
        if (date.isBefore(LocalDate.now())) {
            movies = movieRepository.findByStatusAndReleaseDateAfter(
                    MovieStatus.COMING_SOON,
                    LocalDate.now(),
                    pageable);
            date = LocalDate.now();
        } else {
            movies = movieRepository.findByStatusAndReleaseDateAfter(MovieStatus.COMING_SOON, date, pageable);
        }
        if (movies.isEmpty()) {
            return buildResponse(
                    Page.empty(pageable),
                    ErrorMessages.MOVIES_COMING_SOON_NOT_FOUND + " " + date, HttpStatus.OK);
        }
        return buildResponse(
                movieMapper.mapToResponsePage(movies),
                SuccessMessages.MOVIES_COMING_SOON_FOUND + " " + date,
                HttpStatus.OK);
    }

    public ResponseMessage<Page<MovieResponse>> buildResponse(Page<MovieResponse> body, String message, HttpStatus status) {
        return ResponseMessage.<Page<MovieResponse>>builder()
                .message(message)
                .httpStatus(status)
                .returnBody(body)
                .build();
    }

    public ResponseMessage<Page<MovieResponse>> getAllComingSoon(Pageable pageable) {
        Page<Movie> movies = movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable);
        if (movies.isEmpty()) {
            return buildResponse(
                    Page.empty(pageable),
                    ErrorMessages.MOVIES_COMING_SOON_NOT_FOUND,
                    HttpStatus.OK);
        }
        return buildResponse(
                movieMapper.mapToResponsePage(movies),
                SuccessMessages.MOVIES_COMING_SOON_FOUND,
                HttpStatus.OK);
    }

    public <T> ResponseMessage<Page<T>> buildPageResponse(
            Page<T> body, String message, HttpStatus status) {
        return ResponseMessage.<Page<T>>builder()
                .httpStatus(status)
                .message(message)
                .returnBody(body)
                .build();
    }

    public <T> ResponseMessage<T> buildSingleResponse(
            T body, String message, HttpStatus status) {
        return ResponseMessage.<T>builder()
                .httpStatus(status)
                .message(message)
                .returnBody(body)
                .build();
    }

    // Overload for empty pages
    public <T> ResponseMessage<Page<T>> buildEmptyPageResponse(
            Pageable pageable, String message, HttpStatus status) {
        return buildPageResponse(Page.empty(pageable), message, status);
    }


}
