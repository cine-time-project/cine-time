package com.cinetime.service.imageservice;

import com.cinetime.entity.business.Image;
import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.repository.business.ImageRepository;
import com.cinetime.service.business.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService - GetImageEntity Tests")
class ImageServiceGetImageEntityTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    private Image testImage;

    @BeforeEach
    void setUp() {
        Movie testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .build();

        testImage = Image.builder()
                .id(1L)
                .name("test-image.jpg")
                .type("image/jpeg")
                .data("test image data".getBytes())
                .isPoster(true)
                .movie(testMovie)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("✅ Should return image entity when image exists")
    void shouldReturnImageEntity_whenImageExists() {
        // Given
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));

        // When
        Image result = imageService.getImageEntity(imageId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("test-image.jpg");
        assertThat(result.getType()).isEqualTo("image/jpeg");
        assertThat(result.getData()).isEqualTo("test image data".getBytes());
        assertThat(result.isPoster()).isTrue();
        assertThat(result.getMovie().getId()).isEqualTo(1L);

        verify(imageRepository).findById(imageId);
    }

    @Test
    @DisplayName("❌ Should throw ResourceNotFoundException when image not found")
    void shouldThrowResourceNotFoundException_whenImageNotFound() {
        // Given
        Long imageId = 999L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> imageService.getImageEntity(imageId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessages.IMAGE_NOT_FOUND_ID, imageId));

        verify(imageRepository).findById(imageId);
    }

    @Test
    @DisplayName("💥 Should propagate exception when repository throws error")
    void shouldPropagateException_whenRepositoryThrowsError() {
        // Given
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> imageService.getImageEntity(imageId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(imageRepository).findById(imageId);
    }

    @Test
    @DisplayName("✅ Should handle image with null type")
    void shouldHandleImage_withNullType() {
        // Given
        Long imageId = 1L;
        testImage.setType(null);
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));

        // When
        Image result = imageService.getImageEntity(imageId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("✅ Should handle non-poster image")
    void shouldHandleNonPosterImage() {
        // Given
        Long imageId = 1L;
        testImage.setPoster(false);
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));

        // When
        Image result = imageService.getImageEntity(imageId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isPoster()).isFalse();
    }
}
