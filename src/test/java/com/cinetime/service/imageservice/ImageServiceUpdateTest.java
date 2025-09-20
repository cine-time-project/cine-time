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
@DisplayName("ImageService - Update Tests")
class ImageServiceUpdateTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private ImageService imageService;

    private Movie testMovie;
    private Image existingImage;
    private MultipartFile newFile;
    private ImageResponse updatedResponse;
    private byte[] newFileData;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .build();

        existingImage = Image.builder()
                .id(1L)
                .name("old-image.jpg")
                .type("image/jpeg")
                .data("old data".getBytes())
                .isPoster(false)
                .movie(testMovie)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();

        newFileData = "new image data".getBytes();
        newFile = new MockMultipartFile(
                "file",
                "new-image.png",
                "image/png",
                newFileData
        );

        updatedResponse = ImageResponse.builder()
                .id(1L)
                .name("new-image.png")
                .type("image/png")
                .poster(false)
                .movieId(1L)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now())
                .url("/api/images/1")
                .build();
    }

    @Test
    @DisplayName("✅ Should update image successfully with new file")
    void shouldUpdateImageSuccessfully_withNewFile() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long imageId = 1L;

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(existingImage));
            when(imageRepository.save(existingImage)).thenReturn(existingImage);
            when(imageMapper.toResponse(existingImage)).thenReturn(updatedResponse);

            validator.when(() -> ImageValidator.requireValid(newFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(newFile)).thenReturn(newFileData);
            validator.when(() -> ImageValidator.cleanFileName("new-image.png")).thenReturn("new-image.png");

            // When
            ImageResponse result = imageService.update(imageId, newFile, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("new-image.png");
            assertThat(result.getType()).isEqualTo("image/png");

            verify(imageRepository).findById(imageId);
            verify(imageRepository).save(existingImage);
            verify(imageMapper).toResponse(existingImage);

            // Verify image was updated
            assertThat(existingImage.getName()).isEqualTo("new-image.png");
            assertThat(existingImage.getType()).isEqualTo("image/png");
            assertThat(existingImage.getData()).isEqualTo(newFileData);
        }
    }

    @Test
    @DisplayName("✅ Should update image and set poster flag to true")
    void shouldUpdateImageAndSetPosterFlag_toTrue() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long imageId = 1L;
            Boolean poster = true;

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(existingImage));
            when(imageRepository.existsByMovieIdAndIsPosterTrueAndIdNot(1L, imageId)).thenReturn(false);
            when(imageRepository.save(existingImage)).thenReturn(existingImage);
            when(imageMapper.toResponse(existingImage)).thenReturn(updatedResponse);

            validator.when(() -> ImageValidator.requireValid(newFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(newFile)).thenReturn(newFileData);
            validator.when(() -> ImageValidator.cleanFileName("new-image.png")).thenReturn("new-image.png");

            // When
            ImageResponse result = imageService.update(imageId, newFile, poster);

            // Then
            verify(imageRepository).existsByMovieIdAndIsPosterTrueAndIdNot(1L, imageId);
            assertThat(existingImage.isPoster()).isTrue();
        }
    }

    @Test
    @DisplayName("✅ Should update image and set poster flag to false")
    void shouldUpdateImageAndSetPosterFlag_toFalse() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long imageId = 1L;
            Boolean poster = false;
            existingImage.setPoster(true); // Initially was poster

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(existingImage));
            when(imageRepository.save(existingImage)).thenReturn(existingImage);
            when(imageMapper.toResponse(existingImage)).thenReturn(updatedResponse);

            validator.when(() -> ImageValidator.requireValid(newFile)).thenAnswer(invocation -> null);
            validator.when(() -> ImageValidator.safeBytes(newFile)).thenReturn(newFileData);
            validator.when(() -> ImageValidator.cleanFileName("new-image.png")).thenReturn("new-image.png");

            // When
            imageService.update(imageId, newFile, poster);

            // Then
            assertThat(existingImage.isPoster()).isFalse();
            // Should not check for existing poster when setting to false
            verify(imageRepository, never()).existsByMovieIdAndIsPosterTrueAndIdNot(any(), any());
        }
    }

    @Test
    @DisplayName("❌ Should throw ResourceNotFoundException when image not found")
    void shouldThrowResourceNotFoundException_whenImageNotFound() {
        // Given
        Long imageId = 999L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> imageService.update(imageId, newFile, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessages.IMAGE_NOT_FOUND_ID, imageId));

        verify(imageRepository).findById(imageId);
        verifyNoMoreInteractions(imageRepository);
    }

    @Test
    @DisplayName("❌ Should throw BadRequestException when file is null")
    void shouldThrowBadRequestException_whenFileIsNull() {
        // Given
        Long imageId = 1L;

        // When & Then
        assertThatThrownBy(() -> imageService.update(imageId, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.FILE_MUST_NOT_BE_EMPTY);

        verifyNoInteractions(imageRepository);
    }

    @Test
    @DisplayName("❌ Should throw BadRequestException when file is empty")
    void shouldThrowBadRequestException_whenFileIsEmpty() {
        // Given
        Long imageId = 1L;
        MultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        // When & Then
        assertThatThrownBy(() -> imageService.update(imageId, emptyFile, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.FILE_MUST_NOT_BE_EMPTY);

        verifyNoInteractions(imageRepository);
    }

    @Test
    @DisplayName("❌ Should throw ConflictException when trying to set poster but another poster exists")
    void shouldThrowConflictException_whenAnotherPosterExists() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long imageId = 1L;
            Boolean poster = true;

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(existingImage));
            when(imageRepository.existsByMovieIdAndIsPosterTrueAndIdNot(1L, imageId)).thenReturn(true);

            validator.when(() -> ImageValidator.requireValid(newFile)).thenAnswer(invocation -> null);

            // When & Then
            assertThatThrownBy(() -> imageService.update(imageId, newFile, poster))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(ErrorMessages.MOVIE_ALREADY_HAS_DIFFERENT_POSTER);

            verify(imageRepository).findById(imageId);
            verify(imageRepository).existsByMovieIdAndIsPosterTrueAndIdNot(1L, imageId);
            verify(imageRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("❌ Should throw BadRequestException when file validation fails")
    void shouldThrowBadRequestException_whenFileValidationFails() {
        try (MockedStatic<ImageValidator> validator = mockStatic(ImageValidator.class)) {
            // Given
            Long imageId = 1L;
            validator.when(() -> ImageValidator.requireValid(newFile))
                    .thenThrow(new BadRequestException("Invalid file type"));

            // When & Then
            assertThatThrownBy(() -> imageService.update(imageId, newFile, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid file type");

            verifyNoInteractions(imageRepository);
        }
    }
}
