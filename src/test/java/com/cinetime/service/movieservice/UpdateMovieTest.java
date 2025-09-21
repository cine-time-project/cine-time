package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.Image;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.service.business.CinemaService;
import com.cinetime.service.business.ImageService;
import com.cinetime.service.business.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateMovieTest {

    @InjectMocks
    private MovieService movieService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private CinemaService cinemaService;

    @Mock
    private ImageService imageService;

    private Movie existingMovie;
    private MovieRequest updateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample existing movie
        existingMovie = Movie.builder()
                .id(1L)
                .title("Old Title")
                .slug("old-slug")
                .summary("Old summary")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .status(MovieStatus.COMING_SOON)
                .cast(Collections.singletonList("Old Actor"))
                .formats(Collections.singletonList("2D"))
                .genre(Collections.singletonList("Action"))
                .cinemas(new HashSet<>())
                .images(new HashSet<>())
                .build();

        // Update request with sample data
        updateRequest = MovieRequest.builder()
                .title("New Title")
                .slug("new-slug")
                .summary("New summary")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(150)
                .status(MovieStatus.IN_THEATERS)
                .cast(Collections.singletonList("New Actor"))
                .formats(Collections.singletonList("IMAX"))
                .genre(Collections.singletonList("Drama"))
                .cinemaIds(Set.of(10L, 20L))
                .imageIds(Set.of(100L))
                .build();
    }

    @Test
    void testUpdateMovie_FullUpdate() {
        // Mock DB retrieval
        when(movieRepository.findById(1L)).thenReturn(java.util.Optional.of(existingMovie));

        // Mock cinema and image service
        Cinema cinema1 = new Cinema();
        cinema1.setId(10L);
        Cinema cinema2 = new Cinema();
        cinema2.setId(20L);
        when(cinemaService.getAllByIdIn(updateRequest.getCinemaIds()))
                .thenReturn(Set.of(cinema1, cinema2));

        Image image1 = new Image();
        image1.setId(100L);
        when(imageService.getAllByIdIn(updateRequest.getImageIds()))
                .thenReturn(Set.of(image1));

        // Mock mapper
        doAnswer(invocation -> {
            MovieRequest req = invocation.getArgument(0);
            Movie movie = invocation.getArgument(1);
            movie.setTitle(req.getTitle());
            movie.setSlug(req.getSlug());
            movie.setSummary(req.getSummary());
            movie.setReleaseDate(req.getReleaseDate());
            movie.setDuration(req.getDuration());
            movie.setStatus(req.getStatus());
            movie.setCast(req.getCast());
            movie.setFormats(req.getFormats());
            movie.setGenre(req.getGenre());
            return null;
        }).when(movieMapper).updateMovieFromRequest(updateRequest, existingMovie);

        // Mock response mapping
        MovieResponse mappedResponse = MovieResponse.builder().title("New Title").build();
        when(movieMapper.mapMovieToMovieResponse(any())).thenReturn(mappedResponse);

        // Mock save
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);

        // Execute
        ResponseMessage<MovieResponse> response = movieService.updateMovie(updateRequest, 1L);

        // Assertions
        assertNotNull(response);
        assertEquals("New Title", response.getReturnBody().getTitle());
        assertEquals(2, existingMovie.getCinemas().size());
        assertEquals(1, existingMovie.getImages().size());

        verify(movieRepository).save(existingMovie);
        verify(cinemaService).getAllByIdIn(updateRequest.getCinemaIds());
        verify(imageService).getAllByIdIn(updateRequest.getImageIds());
        verify(movieMapper).updateMovieFromRequest(updateRequest, existingMovie);
    }

    @Test
    void testUpdateMovie_NullCollections() {
        updateRequest.setCinemaIds(null);
        updateRequest.setImageIds(null);

        when(movieRepository.findById(1L)).thenReturn(java.util.Optional.of(existingMovie));
        doNothing().when(movieMapper).updateMovieFromRequest(updateRequest, existingMovie);
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
        when(movieMapper.mapMovieToMovieResponse(existingMovie))
                .thenReturn(MovieResponse.builder().title("Old Title").build());

        ResponseMessage<MovieResponse> response = movieService.updateMovie(updateRequest, 1L);

        // Collections should remain unchanged
        assertEquals(0, existingMovie.getCinemas().size());
        assertEquals(0, existingMovie.getImages().size());
        assertEquals("Old Title", response.getReturnBody().getTitle());
    }

    @Test
    void testUpdateMovie_EmptyCollections() {
        updateRequest.setCinemaIds(Collections.emptySet());
        updateRequest.setImageIds(Collections.emptySet());

        when(movieRepository.findById(1L)).thenReturn(java.util.Optional.of(existingMovie));
        doNothing().when(movieMapper).updateMovieFromRequest(updateRequest, existingMovie);

        // Empty collections should clear existing ones
        when(cinemaService.getAllByIdIn(updateRequest.getCinemaIds()))
                .thenReturn(Collections.emptySet());
        when(imageService.getAllByIdIn(updateRequest.getImageIds()))
                .thenReturn(Collections.emptySet());

        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
        when(movieMapper.mapMovieToMovieResponse(existingMovie))
                .thenReturn(MovieResponse.builder().title("Old Title").build());

        ResponseMessage<MovieResponse> response = movieService.updateMovie(updateRequest, 1L);

        assertEquals(0, existingMovie.getCinemas().size());
        assertEquals(0, existingMovie.getImages().size());
    }
}