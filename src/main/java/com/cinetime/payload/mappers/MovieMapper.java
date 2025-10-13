package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Image;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.ImageResponse;
import com.cinetime.payload.response.business.MovieResponse;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
@Data
public class MovieMapper {

    private final ImageMapper imageMapper;

    /* ===================== Movie -> MovieResponse ===================== */

    public MovieResponse mapMovieToMovieResponse(Movie movie) {
        if (movie == null) return null;

        List<ImageResponse> imageResponses =
                movie.getImages() == null ? List.of()
                        : movie.getImages().stream().map(imageMapper::toResponse).toList();

        Long posterId = resolvePosterId(movie);
        Long heroId   = resolveHeroId(movie, posterId); // scene/backdrop yoksa postere düş

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .summary(movie.getSummary())
                .releaseDate(movie.getReleaseDate())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .formats(movie.getFormats())
                .genre(movie.getGenre())
                .status(movie.getStatus())
                .images(imageResponses)
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .trailerUrl(movie.getTrailerUrl())
                .posterId(posterId)
                .heroId(heroId)
                .build();
    }

    public Page<MovieResponse> mapToResponsePage(Page<Movie> movies) {
        return movies.map(this::mapMovieToMovieResponse);
    }

    /* ===================== Movie -> CinemaMovieResponse ===================== */

    public CinemaMovieResponse mapMovieToCinemaMovieResponse(Movie movie) {
        if (movie == null) return null;

        List<ImageResponse> imageResponses = (movie.getImages() != null)
                ? movie.getImages().stream().map(imageMapper::toResponse).toList()
                : null;

        return CinemaMovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .summary(movie.getSummary())
                .releaseDate(movie.getReleaseDate())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .formats(movie.getFormats())
                .genre(movie.getGenre())
                .status(movie.getStatus())
                .images(imageResponses)
                .trailerUrl(movie.getTrailerUrl())
                .posterUrl(movie.getPosterUrl())
                .build();
    }






    public Page<CinemaMovieResponse> mapToCinemaResponsePage(Page<Movie> movies) {
        return movies.map(this::mapMovieToCinemaMovieResponse);
    }

    /* ===================== MovieRequest <-> Movie ===================== */

    public Movie mapMovieRequestToMovie(MovieRequest req) {
        if (req == null) return null;
        return Movie.builder()
                .title(req.getTitle())
                .summary(req.getSummary())
                .releaseDate(req.getReleaseDate())
                .duration(req.getDuration())
                .rating(req.getRating())
                .specialHalls(req.getSpecialHalls())
                .director(req.getDirector())
                .cast(req.getCast())
                .formats(req.getFormats())
                .genre(req.getGenre())
                .status(req.getStatus())
                // slug, cinemas, images service katmanında set edilecek
                .build();
    }

    public void updateMovieFromRequest(MovieRequest req, Movie movie) {
        if (req == null || movie == null) return;
        movie.setTitle(req.getTitle());
        movie.setSlug(req.getSlug());
        movie.setSummary(req.getSummary());
        movie.setReleaseDate(req.getReleaseDate());
        movie.setDuration(req.getDuration());
        movie.setRating(req.getRating());
        movie.setSpecialHalls(req.getSpecialHalls());
        movie.setDirector(req.getDirector());
        movie.setCast(req.getCast());
        movie.setFormats(req.getFormats());
        movie.setGenre(req.getGenre());
        movie.setStatus(req.getStatus());
        // cinemas & images service katmanında yönetilecek
    }

    /* ===================== Helpers (sen istersen ayrı sınıfa taşırsın) ===================== */

    private Long resolvePosterId(Movie m) {
        if (m.getImages() == null) return null;
        return m.getImages().stream()
                .filter(this::isPosterTrue)        // isPoster = true
                .map(Image::getId)
                .findFirst()
                .orElse(null);
    }

    private Long resolveHeroId(Movie m, Long fallbackPosterId) {
        if (m.getImages() != null) {
            Long sceneId = m.getImages().stream()
                    .filter(img -> !isPosterTrue(img))   // isPoster = false (scene/backdrop)
                    .map(Image::getId)
                    .findFirst()
                    .orElse(null);
            if (sceneId != null) return sceneId;
        }
        return fallbackPosterId; // sahne yoksa postere düş
    }

    private boolean isPosterTrue(Image img) {
        // Entity'inde Boolean getIsPoster() veya boolean isPoster() olabilir; ikisini de destekle
        try {
            Boolean val = (Boolean) img.getClass().getMethod("getIsPoster").invoke(img);
            return Boolean.TRUE.equals(val);
        } catch (ReflectiveOperationException ignored) {
            try {
                Object val = img.getClass().getMethod("isPoster").invoke(img);
                return (val instanceof Boolean) && (Boolean) val;
            } catch (ReflectiveOperationException ignored2) {
                return false;
            }
        }
    }

    public MovieStatus movieStatusMapper(String status){
        if (status == null || status.isBlank()) return null;

        MovieStatus movieStatus = null;

        if (status.trim().equalsIgnoreCase(MovieStatus.IN_THEATERS.toString())) {
            movieStatus = MovieStatus.IN_THEATERS;
        } else if (status.trim().equalsIgnoreCase(MovieStatus.COMING_SOON.toString())) {
            movieStatus = MovieStatus.COMING_SOON;
        } else if (status.trim().equalsIgnoreCase(MovieStatus.PRESALE.toString())) {
            movieStatus = MovieStatus.PRESALE;
        }

        return movieStatus;
    }
}