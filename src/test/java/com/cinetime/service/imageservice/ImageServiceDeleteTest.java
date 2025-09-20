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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService - Delete Tests")
class ImageServiceDeleteTest {

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
                .data("test data".getBytes())
                .isPoster(false)
                .movie(testMovie)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("✅ Should delete image successfully when image exists")
    void shouldDeleteImageSuccessfully_whenImageExists() {
        // Given
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));
        doNothing().when(imageRepository).delete(testImage);

        // When
        imageService.delete(imageId);

        // Then
        verify(imageRepository).findById(imageId);
        verify(imageRepository).delete(testImage);
    }

    @Test
    @DisplayName("❌ Should throw ResourceNotFoundException when image not found")
    void shouldThrowResourceNotFoundException_whenImageNotFound() {
        // Given
        Long imageId = 999L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> imageService.delete(imageId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessages.IMAGE_NOT_FOUND_ID, imageId));

        verify(imageRepository).findById(imageId);
        verify(imageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("💥 Should propagate exception when repository delete fails")
    void shouldPropagateException_whenRepositoryDeleteFails() {
        // Given
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));
        doThrow(new RuntimeException("Database error")).when(imageRepository).delete(testImage);

        // When & Then
        assertThatThrownBy(() -> imageService.delete(imageId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(imageRepository).findById(imageId);
        verify(imageRepository).delete(testImage);
    }
}
