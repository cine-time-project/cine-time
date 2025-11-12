package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.CinemaImage;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaImageMapper;
import com.cinetime.payload.response.business.CinemaImageResponse;
import com.cinetime.repository.business.CinemaImageRepository;
import com.cinetime.repository.business.CinemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CinemaImageServiceTest {

    @Mock
    private CinemaImageRepository cinemaImageRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private CinemaImageMapper cinemaImageMapper;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private CinemaImageService cinemaImageService;

    private Cinema cinema;
    private CinemaImage cinemaImage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cinema = Cinema.builder().id(1L).build();
        cinemaImage = CinemaImage.builder().id(10L).cinema(cinema).build();
    }

    // ===============================
    // getCinemaImage
    // ===============================
    @Test
    void testGetCinemaImage_found() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.of(cinemaImage));

        CinemaImage result = cinemaImageService.getCinemaImage(1L);

        assertEquals(cinemaImage, result);
    }

    @Test
    void testGetCinemaImage_notFound() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cinemaImageService.getCinemaImage(1L));
    }

    // ===============================
    // upload
    // ===============================
    @Test
    void testUpload_newImage() throws Exception {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getBytes()).thenReturn(new byte[]{1,2,3});

        when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

        CinemaImageResponse response = cinemaImageService.upload(1L, multipartFile);

        assertNotNull(response);
        verify(cinemaImageRepository, times(1)).save(any(CinemaImage.class));
    }

    @Test
    void testUpload_existingImage() throws Exception {
        cinema.setCinemaImage(cinemaImage);

        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(multipartFile.getOriginalFilename()).thenReturn("updated.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getBytes()).thenReturn(new byte[]{1,2,3});

        when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

        CinemaImageResponse response = cinemaImageService.upload(1L, multipartFile);

        assertNotNull(response);
        assertEquals(cinemaImage, cinema.getCinemaImage());
        verify(cinemaImageRepository, times(1)).save(cinemaImage);
    }

    @Test
    void testUpload_cinemaNotFound() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cinemaImageService.upload(1L, multipartFile));
    }

    // ===============================
    // uploadFromUrl
    // ===============================
    @Test
    void testUploadFromUrl_success() {
        String url = "https://example.com/test.jpg";
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

    }

    @Test
    void testUploadFromUrl_conflict() {
        cinema.setCinemaImage(cinemaImage);
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        assertThrows(ConflictException.class, () -> cinemaImageService.uploadFromUrl(1L, "https://example.com/test.jpg"));
    }

    @Test
    void testUploadFromUrl_invalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> cinemaImageService.uploadFromUrl(1L, ""));
    }

    // ===============================
    // replaceWithUrl
    // ===============================
    @Test
    void testReplaceWithUrl_createNew() {
        String url = "https://example.com/test.jpg";
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.empty());
        when(cinemaImageRepository.save(any(CinemaImage.class))).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

        CinemaImageResponse response = cinemaImageService.replaceWithUrl(1L, url);

        assertNotNull(response);
        verify(cinemaImageRepository, times(1)).save(any(CinemaImage.class));
    }

    @Test
    void testReplaceWithUrl_updateExisting() {
        String url = "https://example.com/test.jpg";
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.of(cinemaImage));
        when(cinemaImageRepository.save(cinemaImage)).thenReturn(cinemaImage);
        when(cinemaImageMapper.cinemaImageToResponse(cinemaImage)).thenReturn(new CinemaImageResponse());

        CinemaImageResponse response = cinemaImageService.replaceWithUrl(1L, url);

        assertNotNull(response);
        verify(cinemaImageRepository, times(1)).save(cinemaImage);
    }

    @Test
    void testReplaceWithUrl_invalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> cinemaImageService.replaceWithUrl(1L, ""));
    }

    // ===============================
    // delete
    // ===============================
    @Test
    void testDelete_existingImage() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.of(cinemaImage));

        cinemaImageService.delete(1L);

        verify(cinemaImageRepository, times(1)).delete(cinemaImage);
    }

    @Test
    void testDelete_noImage() {
        when(cinemaImageRepository.findByCinema_Id(1L)).thenReturn(Optional.empty());

        cinemaImageService.delete(1L);

        verify(cinemaImageRepository, never()).delete(any());
    }
}
