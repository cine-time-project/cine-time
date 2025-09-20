package com.cinetime.service.imageservice;

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
import com.cinetime.service.business.ImageService;
import com.cinetime.service.validator.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService - Upload Tests")
class ImageServiceUploadTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private ImageService imageService;

    private Movie testMovie;
    private MultipartFile testFile;
    private Image testImage;
    private ImageResponse testImageResponse;
    private byte[] testFileData;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .slug("test-movie")
                .build();

        testFileData = "test image data".getBytes();
        testFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                testFileData
        );

        testImage = Image.builder()
                .id(1L)
                .name("test-image.jpg")
                .type("image/jpeg")
                .data(testFileData)
                .isPoster(false)
                .movie(testMovie)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testImageResponse = ImageResponse.builder()
                .id(1L)
                .name("test-image.jpg")
                .type("image/jpeg")
                .poster(false)
                .movieId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .url("/api/images/1")
                .build();
    }

    @Test
    @DisplayName("✅ Should upload image successfully when valid file and movie (non-poster)")
    void shouldUploadImageSuccessfully_whenValidFileAndMovie() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            boolean poster = false; // This is key - when false, existsByMovieIdAndIsPosterTrue is NOT called

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
            // DON'T stub existsByMovieIdAndIsPosterTrue when poster=false!
            when(imageRepository.save(any(Image.class))).thenReturn(testImage);
            when(imageMapper.toResponse(testImage)).thenReturn(testImageResponse);

            validator.when(() -> ImageValidator.requireValid(testFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(testFile)).thenReturn(testFileData);
            validator.when(() -> ImageValidator.cleanFileName("test-image.jpg")).thenReturn("test-image.jpg");

            // When
            ImageResponse result = imageService.upload(movieId, testFile, poster);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("test-image.jpg");
            assertThat(result.getType()).isEqualTo("image/jpeg");
            assertThat(result.isPoster()).isFalse();
            assertThat(result.getMovieId()).isEqualTo(1L);

            verify(movieRepository).findById(movieId);
            verify(imageRepository).save(any(Image.class));
            verify(imageMapper).toResponse(testImage);

            // Verify validator methods were called
            validator.verify(() -> ImageValidator.requireValid(testFile));
            validator.verify(() -> ImageValidator.safeBytes(testFile));
            validator.verify(() -> ImageValidator.cleanFileName("test-image.jpg"));

            // Verify poster check was NOT called
            verify(imageRepository, never()).existsByMovieIdAndIsPosterTrue(any());
        }
    }

    @Test
    @DisplayName("✅ Should upload poster successfully when no existing poster")
    void shouldUploadPosterSuccessfully_whenNoExistingPoster() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            boolean poster = true; // Only when true, the poster check is called

            Image posterImage = Image.builder()
                    .id(1L)
                    .name("poster.jpg")
                    .type("image/jpeg")
                    .data(testFileData)
                    .isPoster(true)
                    .movie(testMovie)
                    .build();

            ImageResponse posterResponse = ImageResponse.builder()
                    .id(1L)
                    .name("poster.jpg")
                    .poster(true)
                    .movieId(1L)
                    .build();

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
            when(imageRepository.existsByMovieIdAndIsPosterTrue(movieId)).thenReturn(false);
            when(imageRepository.save(any(Image.class))).thenReturn(posterImage);
            when(imageMapper.toResponse(posterImage)).thenReturn(posterResponse);

            validator.when(() -> ImageValidator.requireValid(testFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(testFile)).thenReturn(testFileData);
            validator.when(() -> ImageValidator.cleanFileName("test-image.jpg")).thenReturn("poster.jpg");

            // When
            ImageResponse result = imageService.upload(movieId, testFile, poster);

            // Then
            assertThat(result.isPoster()).isTrue();
            verify(movieRepository).findById(movieId);
            verify(imageRepository).existsByMovieIdAndIsPosterTrue(movieId);
            verify(imageRepository).save(any(Image.class));
            verify(imageMapper).toResponse(posterImage);
        }
    }

    @Test
    @DisplayName("❌ Should throw ResourceNotFoundException when movie not found")
    void shouldThrowResourceNotFoundException_whenMovieNotFound() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 999L;
            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());
            validator.when(() -> ImageValidator.requireValid(testFile)).thenAnswer(invocation -> null);

            // When & Then
            assertThatThrownBy(() -> imageService.upload(movieId, testFile, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(String.format(ErrorMessages.MOVIE_NOT_FOUND_ID, movieId));

            verify(movieRepository).findById(movieId);
            verifyNoInteractions(imageRepository);
            verifyNoInteractions(imageMapper);
        }
    }

    @Test
    @DisplayName("❌ Should throw ConflictException when trying to upload poster but one already exists")
    void shouldThrowConflictException_whenPosterAlreadyExists() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            boolean poster = true;

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
            when(imageRepository.existsByMovieIdAndIsPosterTrue(movieId)).thenReturn(true);
            validator.when(() -> ImageValidator.requireValid(testFile)).thenAnswer(invocation -> null);

            // When & Then
            assertThatThrownBy(() -> imageService.upload(movieId, testFile, poster))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(ErrorMessages.MOVIE_ALREADY_HAS_POSTER);

            verify(movieRepository).findById(movieId);
            verify(imageRepository).existsByMovieIdAndIsPosterTrue(movieId);
            verify(imageRepository, never()).save(any());
            verifyNoInteractions(imageMapper);
        }
    }

    @Test
    @DisplayName("❌ Should throw BadRequestException when file is invalid")
    void shouldThrowBadRequestException_whenFileIsInvalid() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            validator.when(() -> ImageValidator.requireValid(testFile))
                    .thenThrow(new BadRequestException(ErrorMessages.FILE_MUST_NOT_BE_EMPTY));

            // When & Then
            assertThatThrownBy(() -> imageService.upload(movieId, testFile, false))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.FILE_MUST_NOT_BE_EMPTY);

            // Verify that validator.requireValid was called and threw exception
            validator.verify(() -> ImageValidator.requireValid(testFile));

            // These should not be called because exception is thrown early
            verifyNoInteractions(movieRepository);
            verifyNoInteractions(imageRepository);
            verifyNoInteractions(imageMapper);
        }
    }

    @Test
    @DisplayName("❌ Should propagate validator exception when file reading fails")
    void shouldPropagateException_whenFileReadingFails() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            boolean poster = false; // Important: poster=false so no poster check

            // Mock the flow up to the point where safeBytes fails
            when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
            // DON'T stub poster check when poster=false

            validator.when(() -> ImageValidator.requireValid(testFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(testFile))
                    .thenThrow(new BadRequestException("could not read uploaded file"));

            // When & Then
            assertThatThrownBy(() -> imageService.upload(movieId, testFile, poster))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("could not read uploaded file");

            // Verify the flow
            verify(movieRepository).findById(movieId);
            // DON'T verify poster check when poster=false
            verify(imageRepository, never()).existsByMovieIdAndIsPosterTrue(any());

            validator.verify(() -> ImageValidator.requireValid(testFile));
            validator.verify(() -> ImageValidator.safeBytes(testFile));

            // These should not be called because exception is thrown
            verify(imageRepository, never()).save(any());
            verifyNoInteractions(imageMapper);
        }
    }

    @Test
    @DisplayName("❌ Should handle empty file")
    void shouldThrowBadRequestException_whenFileIsEmpty() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            MultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

            validator.when(() -> ImageValidator.requireValid(emptyFile))
                    .thenThrow(new BadRequestException(ErrorMessages.FILE_MUST_NOT_BE_EMPTY));

            // When & Then
            assertThatThrownBy(() -> imageService.upload(movieId, emptyFile, false))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.FILE_MUST_NOT_BE_EMPTY);

            validator.verify(() -> ImageValidator.requireValid(emptyFile));
            verifyNoInteractions(movieRepository);
            verifyNoInteractions(imageRepository);
        }
    }

    @Test
    @DisplayName("❌ Should handle null file")
    void shouldThrowBadRequestException_whenFileIsNull() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            MultipartFile nullFile = null;

            validator.when(() -> ImageValidator.requireValid(nullFile))
                    .thenThrow(new BadRequestException(ErrorMessages.FILE_MUST_NOT_BE_EMPTY));

            // When & Then
            assertThatThrownBy(() -> imageService.upload(movieId, nullFile, false))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.FILE_MUST_NOT_BE_EMPTY);

            validator.verify(() -> ImageValidator.requireValid(nullFile));
            verifyNoInteractions(movieRepository);
            verifyNoInteractions(imageRepository);
        }
    }

    @Test
    @DisplayName("✅ Should handle file with special characters in name")
    void shouldHandleFileWithSpecialCharacters() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            boolean poster = false; // Key: non-poster so no poster check
            MultipartFile specialFile = new MockMultipartFile(
                    "file",
                    "test@#$%^&*()image.jpg",
                    "image/jpeg",
                    testFileData
            );

            String cleanedName = "test_________image.jpg";

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
            // DON'T stub poster check when poster=false
            when(imageRepository.save(any(Image.class))).thenReturn(testImage);
            when(imageMapper.toResponse(testImage)).thenReturn(testImageResponse);

            validator.when(() -> ImageValidator.requireValid(specialFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(specialFile)).thenReturn(testFileData);
            validator.when(() -> ImageValidator.cleanFileName("test@#$%^&*()image.jpg")).thenReturn(cleanedName);

            // When
            ImageResponse result = imageService.upload(movieId, specialFile, poster);

            // Then
            assertThat(result).isNotNull();
            validator.verify(() -> ImageValidator.cleanFileName("test@#$%^&*()image.jpg"));
            verify(imageRepository, never()).existsByMovieIdAndIsPosterTrue(any());
        }
    }

    @Test
    @DisplayName("💥 Should propagate repository save exception")
    void shouldPropagateRepositorySaveException() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long movieId = 1L;
            boolean poster = false; // Key: non-poster

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
            // DON'T stub poster check when poster=false
            when(imageRepository.save(any(Image.class))).thenThrow(new RuntimeException("Database error"));

            validator.when(() -> ImageValidator.requireValid(testFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(testFile)).thenReturn(testFileData);
            validator.when(() -> ImageValidator.cleanFileName("test-image.jpg")).thenReturn("test-image.jpg");

            // When & Then
            assertThatThrownBy(() -> imageService.upload(movieId, testFile, poster))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(movieRepository).findById(movieId);
            verify(imageRepository).save(any(Image.class));
            verify(imageRepository, never()).existsByMovieIdAndIsPosterTrue(any());
            verifyNoInteractions(imageMapper);
        }
    }
}
