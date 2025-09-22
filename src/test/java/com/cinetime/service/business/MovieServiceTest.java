//package com.cinetime.service.business;
//
//import com.cinetime.entity.business.Movie;
//import com.cinetime.exception.ResourceNotFoundException;
//import com.cinetime.payload.mappers.MovieMapper;
//import com.cinetime.payload.messages.ErrorMessages;
//import com.cinetime.payload.messages.SuccessMessages;
//import com.cinetime.payload.response.business.CinemaMovieResponse;
//import com.cinetime.payload.response.business.MovieResponse;
//import com.cinetime.payload.response.business.ResponseMessage;
//import com.cinetime.repository.business.MovieRepository;
//import com.cinetime.service.helper.PageableHelper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.data.domain.*;
//import org.springframework.http.HttpStatus;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class MovieServiceTest {
//
//  @Mock
//  private MovieRepository movieRepository;
//
//  @Mock
//  private PageableHelper pageableHelper;
//
//  @Mock
//  private MovieMapper movieMapper;
//
//  @InjectMocks
//  private MovieService movieService;
//
//  private Pageable pageable;
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//    pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
//    when(pageableHelper.buildPageable(anyInt(), anyInt(), anyString(), anyString()))
//        .thenReturn(pageable);
//  }
//
//  @Test
//  void searchMovies_withKeyword_returnsFilteredResults() {
//    // given
//    Movie movie = new Movie();
//    Page<Movie> moviePage = new PageImpl<>(List.of(movie));
//    when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(eq("batman"), eq("batman"), eq(pageable)))
//        .thenReturn(moviePage);
//
//    Page<MovieResponse> mappedPage = new PageImpl<>(List.of(new MovieResponse()));
//    when(movieMapper.mapToResponsePage(moviePage)).thenReturn(mappedPage);
//
//    // when
//    ResponseMessage<Page<MovieResponse>> result =
//        movieService.searchMovies("batman", 0, 10, "id", "asc");
//
//    // then
//    assertEquals(HttpStatus.OK, result.getHttpStatus());
//    assertEquals("Movies have been found successfully", result.getMessage());
//    assertFalse(result.getReturnBody().isEmpty());
//  }
//
//  @Test
//  void searchMovies_withoutKeyword_returnsAllResults() {
//    // given
//    Page<Movie> moviePage = new PageImpl<>(List.of(new Movie()));
//    when(movieRepository.findAll(pageable)).thenReturn(moviePage);
//
//    Page<MovieResponse> mappedPage = new PageImpl<>(List.of(new MovieResponse()));
//    when(movieMapper.mapToResponsePage(moviePage)).thenReturn(mappedPage);
//
//    // when
//    ResponseMessage<Page<MovieResponse>> result =
//        movieService.searchMovies(" ", 0, 10, "id", "asc");
//
//    // then
//    assertEquals(HttpStatus.OK, result.getHttpStatus());
//    assertEquals("Movies have been found successfully", result.getMessage());
//  }
//
////  @Test
////  void findMoviesByCinemaSlug_validSlug_returnsMovies() {
////    // given
////    String slug = "cinema-slug";
////    Page<Movie> moviePage = new PageImpl<>(List.of(new Movie()));
////    Page<CinemaMovieResponse> mappedPage = new PageImpl<>(List.of(new CinemaMovieResponse()));
////
////    when(movieRepository.findAllBySlugIgnoreCase(slug, pageable)).thenReturn(moviePage);
////    when(movieMapper.mapToCinemaResponsePage(moviePage)).thenReturn(mappedPage);
////
////    // when
////    ResponseMessage<Page<CinemaMovieResponse>> result =
////        movieService.findMoviesByCinemaSlug(slug, 0, 10, "id", "asc");
////
////    // then
////    assertEquals(HttpStatus.OK, result.getHttpStatus());
////    assertTrue(result.getMessage().contains(slug));
////    assertFalse(result.getReturnBody().isEmpty());
////  }
//
//  @Test
//  void findMoviesByCinemaSlug_emptySlug_throwsException() {
//    assertThrows(IllegalArgumentException.class,
//        () -> movieService.findMoviesByCinemaSlug("  ", 0, 10, "id", "asc"));
//  }
//
//  @Test
//  void findMoviesByCinemaSlug_noResults_returnsNotFound() {
//    // given
//    String slug = "not-found";
//    Page<Movie> emptyPage = Page.empty();
//    when(movieRepository.findAllBySlugIgnoreCase(slug, pageable)).thenReturn(emptyPage);
//    when(movieMapper.mapToCinemaResponsePage(emptyPage)).thenReturn(Page.empty());
//
//    // when
//    ResponseMessage<Page<CinemaMovieResponse>> result =
//        movieService.findMoviesByCinemaSlug(slug, 0, 10, "id", "asc");
//
//    // then
//    assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
//    assertEquals(ErrorMessages.MOVIE_NOT_FOUND, result.getMessage());
//    assertNull(result.getReturnBody());
//  }
//
//  @Test
//  void getMovieById_existingId_returnsMovie() {
//    // given
//    Movie movie = new Movie();
//    movie.setId(1L);
//    when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
//
//    MovieResponse response = new MovieResponse();
//    when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(response);
//
//    // when
//    ResponseMessage<MovieResponse> result = movieService.getMovieById(1L);
//
//    // then
//    assertEquals(HttpStatus.OK, result.getHttpStatus());
//    assertEquals(String.format(SuccessMessages.MOVIE_WITH_ID_FOUND, 1L), result.getMessage());
//    assertNotNull(result.getReturnBody());
//  }
//
//  @Test
//  void getMovieById_notExistingId_throwsResourceNotFoundException() {
//    // given
//    when(movieRepository.findById(99L)).thenReturn(Optional.empty());
//
//    // then
//    ResourceNotFoundException ex =
//        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(99L));
//
//    assertTrue(ex.getMessage().contains("99"));
//  }
//}
