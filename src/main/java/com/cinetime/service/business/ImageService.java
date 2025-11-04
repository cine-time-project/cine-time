package com.cinetime.service.business;

import com.cinetime.entity.business.Image;
import com.cinetime.entity.business.Movie;
import com.cinetime.exception.BadRequestException;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.ImageMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.ImageResponse;
import com.cinetime.repository.business.ImageRepository;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.validator.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MovieRepository movieRepository;
    private final ImageMapper imageMapper;

    /**
     * I01 helper — returns the entity with bytes to be streamed by the controller.
     */
    @Transactional(readOnly = true)
    public Image getImageEntity(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.IMAGE_NOT_FOUND_ID, imageId)));
    }

    @Transactional(readOnly = true)
    public Set<Image> getAllByIdIn(Set<Long> ids) {
        Set<Image> images = imageRepository.findAllByIdIn(ids);
        if (images.isEmpty()) throw new ResourceNotFoundException(ErrorMessages.IMAGE_NOT_FOUND);
        return images;
    }

    /**
     * I02 — Upload a new image for a movie.
     */
    @Transactional
    public ImageResponse upload(Long movieId, MultipartFile file, boolean poster) {
        ImageValidator.requireValid(file);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.MOVIE_NOT_FOUND_ID, movieId)));

        if (poster && imageRepository.existsByMovieIdAndIsPosterTrue(movieId)) {
            throw new ConflictException(ErrorMessages.MOVIE_ALREADY_HAS_POSTER);
        }

        Image img = Image.builder()
                .movie(movie)
                .data(ImageValidator.safeBytes(file))
                .name(ImageValidator.cleanFileName(file.getOriginalFilename()))
                .type(file.getContentType())
                .isPoster(poster)
                .build();

        Image saved = imageRepository.save(img);
        return imageMapper.toResponse(saved);
    }

    /**
     * I03 — Delete an image by id.
     */
    @Transactional
    public void delete(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.IMAGE_NOT_FOUND_ID, imageId)));
        imageRepository.delete(image);
    }

    /**
     * I04 — Update image bytes and optionally toggle poster flag.
     */
    @Transactional
    public ImageResponse update(Long imageId, MultipartFile file, Boolean poster) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorMessages.FILE_MUST_NOT_BE_EMPTY);
        }
        ImageValidator.requireValid(file);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.IMAGE_NOT_FOUND_ID, imageId)));

        if (Boolean.TRUE.equals(poster)) {
            Long movieId = image.getMovie().getId();
            boolean anotherPoster = imageRepository
                    .existsByMovieIdAndIsPosterTrueAndIdNot(movieId, imageId);
            if (anotherPoster) {
                throw new ConflictException(ErrorMessages.MOVIE_ALREADY_HAS_DIFFERENT_POSTER);
            }
        }

        image.setData(ImageValidator.safeBytes(file));
        image.setName(ImageValidator.cleanFileName(file.getOriginalFilename()));
        image.setType(file.getContentType());
        if (poster != null) {
            image.setPoster(poster);
        }

        Image saved = imageRepository.save(image);
        return imageMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<Image> findImagesByMovieId(Long movieId) {
        return imageRepository.findByMovie_IdOrderByCreatedAtDesc(movieId);
    }

    @Transactional(readOnly = true)
    public Long findPosterIdByMovieId(Long movieId) {
        return imageRepository.findPosterImageIdByMovieId(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Poster not found for movie ID: %s", movieId)));
    }

    /**
     * Get all images with pagination and filtering
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ImageResponse> getAllImagesPaginated(
            int page, int size, String sort, String type, String q, Long movieId) {

        // Create sort direction
        var direction = type.equalsIgnoreCase("asc")
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;

        // Create pageable
        var pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(direction, sort)
        );

        // Get images based on filters
        org.springframework.data.domain.Page<Image> images;

        if (movieId != null) {
            images = imageRepository.findByMovieId(movieId, pageable);
        } else if (q != null && !q.isEmpty()) {
            images = imageRepository.findByNameContainingIgnoreCase(q, pageable);
        } else {
            images = imageRepository.findAll(pageable);
        }

        // Convert to ImageResponse
        return images.map(imageMapper::toResponse);
    }
}