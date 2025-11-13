package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.CinemaImage;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaImageMapper;
import com.cinetime.payload.response.business.CinemaImageResponse;
import com.cinetime.repository.business.CinemaImageRepository;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.service.validator.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaImageServiceTest {

    @Mock private CinemaImageRepository cinemaImageRepository;
    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaImageMapper cinemaImageMapper;
    @Mock private MultipartFile multipartFile;

    @InjectMocks
    private CinemaImageService cinemaImageService;

    private Cinema cinema;
    private CinemaImage cinemaImage;

    @BeforeEach
    void init() {
        cinema = Cinema.builder().id(1L).build();
        cinemaImage = CinemaImage.builder().id(10L).cinema(cinema).build();
    }

    // ================= getCinemaImage =================
    @Test
    void getCinemaImage_found() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.of(cinemaImage));

        CinemaImage result = cinemaImageService.getCinemaImage(1L);

        assertEquals(cinemaImage, result);
    }

    @Test
    void getCinemaImage_notFound_throws() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cinemaImageService.getCinemaImage(1L));
    }

    // ================= upload (multipart) =================
    @Test
    void upload_newImage_success() throws Exception {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        try (MockedStatic<ImageValidator> st = mockStatic(ImageValidator.class)) {
            st.when(() -> ImageValidator.requireValid(any(MultipartFile.class)))
                    .thenAnswer(inv -> null); // no-op

            when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
            when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

            CinemaImageResponse resp = cinemaImageService.upload(1L, multipartFile);

            assertNotNull(resp);
            verify(cinemaImageRepository).save(any(CinemaImage.class));
        }
    }

    @Test
    void upload_existingImage_updatesSameRow() throws Exception {
        cinema.setCinemaImage(cinemaImage);
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        try (MockedStatic<ImageValidator> st = mockStatic(ImageValidator.class)) {
            st.when(() -> ImageValidator.requireValid(any(MultipartFile.class)))
                    .thenAnswer(inv -> null); // no-op

            when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
            when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

            CinemaImageResponse resp = cinemaImageService.upload(1L, multipartFile);

            assertNotNull(resp);
            verify(cinemaImageRepository).save(any(CinemaImage.class));
        }
    }

    @Test
    void upload_cinemaNotFound_throws() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<ImageValidator> st = mockStatic(ImageValidator.class)) {
            st.when(() -> ImageValidator.requireValid(any(MultipartFile.class)))
                    .thenAnswer(inv -> null);

            assertThrows(ResourceNotFoundException.class,
                    () -> cinemaImageService.upload(1L, multipartFile));
        }
    }

    // ================= uploadFromUrl =================
      @Test
    void uploadFromUrl_success() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

        // Use a URL that returns HTTP 200 + an image
        String url = "https://httpbin.org/image/png";

        CinemaImageResponse resp = cinemaImageService.uploadFromUrl(1L, url);

        assertNotNull(resp);
        verify(cinemaImageRepository).save(any(CinemaImage.class));
    }
  
    @Test
    void uploadFromUrl_conflict_whenAlreadyHasImage() {
        cinema.setCinemaImage(cinemaImage);
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        assertThrows(ConflictException.class,
                () -> cinemaImageService.uploadFromUrl(1L, "https://x/y/poster.jpg"));
    }

    @Test
    void uploadFromUrl_invalidUrl_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> cinemaImageService.uploadFromUrl(1L, " "));
    }

    // ================= replaceWithUrl =================
    @Test
    void replaceWithUrl_createNew_whenNoneExists() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.empty());
        when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

        CinemaImageResponse resp = cinemaImageService.replaceWithUrl(1L, "https://x/y/cover.png");

        assertNotNull(resp);
        verify(cinemaImageRepository).save(any(CinemaImage.class));
    }

    @Test
    void replaceWithUrl_updateExisting() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.of(cinemaImage));
        when(cinemaImageRepository.save(cinemaImage)).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

        CinemaImageResponse resp = cinemaImageService.replaceWithUrl(1L, "https://x/y/cover.png");

        assertNotNull(resp);
        verify(cinemaImageRepository).save(cinemaImage);
    }

    @Test
    void replaceWithUrl_invalidUrl_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> cinemaImageService.replaceWithUrl(1L, ""));
    }

    // ================= delete =================
    @Test
    void delete_existing_deletes() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.of(cinemaImage));

        cinemaImageService.delete(1L);

        verify(cinemaImageRepository).delete(cinemaImage);
    }

    @Test
    void delete_none_noop() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.empty());

        cinemaImageService.delete(1L);

        verify(cinemaImageRepository, never()).delete(any());
    }
}
