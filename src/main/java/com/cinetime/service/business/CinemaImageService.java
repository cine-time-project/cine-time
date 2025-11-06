package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.CinemaImage;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaImageMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.CinemaImageResponse;
import com.cinetime.repository.business.CinemaImageRepository;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.service.validator.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CinemaImageService {

    private final CinemaImageRepository cinemaImageRepository;
    private final CinemaRepository cinemaRepository;
    private final CinemaImageMapper cinemaImageMapper;


    @Transactional(readOnly = true)
    public CinemaImage getCinemaImage(Long cinemaId) {
        return cinemaImageRepository.findByCinema_Id(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.IMAGE_NOT_FOUND_ID, cinemaId)));
    }

    @Transactional
    public CinemaImageResponse upload(Long cinemaId, MultipartFile file ) {
        ImageValidator.requireValid(file);

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.CINEMA_NOT_FOUND, cinemaId)));

        CinemaImage cinemaImage = cinema.getCinemaImage();

        try {
            if (cinemaImage == null) {
                // Eğer mevcut image yoksa, yeni bir tane oluştur
                cinemaImage = CinemaImage.builder()
                        .cinema(cinema)
                        .name(file.getOriginalFilename())
                        .type(file.getContentType())
                        .data(file.getBytes())
                        .build();
            } else {
                // Mevcut image varsa üzerine yaz
                cinemaImage.setName(file.getOriginalFilename());
                cinemaImage.setType(file.getContentType());
                cinemaImage.setData(file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read image bytes", e);
        }

        CinemaImage saved = cinemaImageRepository.save(cinemaImage);
        return cinemaImageMapper.cinemaImageToResponse(saved);
    }

    @Transactional
    public CinemaImageResponse uploadFromUrl(Long cinemaId, String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }

        // Validate cinema existence
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.CINEMA_NOT_FOUND, cinemaId)));

        if (cinema.getCinemaImage() != null) {
            throw new ConflictException("Cinema already has an image");
        }

        try {
            java.net.URI uri = java.net.URI.create(url);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(uri).GET().build();
            java.net.http.HttpResponse<byte[]> res = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() >= 400) {
                throw new RuntimeException("Failed to fetch image. HTTP status: " + res.statusCode());
            }

            String contentType = res.headers().firstValue("content-type")
                    .orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String fileName = java.nio.file.Paths.get(uri.getPath()).getFileName().toString();
            if (fileName == null || fileName.isBlank()) {
                fileName = "image";
            }

            CinemaImage cinemaImage = CinemaImage.builder()
                    .cinema(cinema)
                    .name(fileName)
                    .type(contentType)
                    .data(res.body())
                    .url(url)
                    .build();

            CinemaImage saved = cinemaImageRepository.save(cinemaImage);
            return cinemaImageMapper.cinemaImageToResponse(saved);
        } catch (Exception e) {
            throw new RuntimeException("Could not download image from url", e);
        }
    }

    /**
     * Replace (or create) the image of a cinema with an external URL.
     * Unlike POST, this method is idempotent and will update the existing
     * CinemaImage row if it already exists.
     */
    @Transactional
    public CinemaImageResponse replaceWithUrl(Long cinemaId, String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }

        // Ensure cinema exists
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.CINEMA_NOT_FOUND, cinemaId)));

        // Try to reuse/modify existing record; if absent create a new one
        CinemaImage img = cinemaImageRepository.findByCinema_Id(cinemaId)
                .orElse(CinemaImage.builder().cinema(cinema).build());

        // Best-effort content type + file name from URL
        String contentType = org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
        try {
            java.net.URI uri = java.net.URI.create(url);
            String fn = java.nio.file.Paths.get(uri.getPath()).getFileName().toString();
            if (fn != null && !fn.isBlank()) {
                img.setName(fn);
                if (fn.toLowerCase().endsWith(".png")) contentType = org.springframework.http.MediaType.IMAGE_PNG_VALUE;
                else if (fn.toLowerCase().endsWith(".webp")) contentType = "image/webp";
                else if (fn.toLowerCase().endsWith(".gif")) contentType = org.springframework.http.MediaType.IMAGE_GIF_VALUE;
            }
        } catch (Exception ignored) {}

        img.setType(contentType);
        img.setUrl(url);
        // We prefer the external URL; clear binary bytes
        img.setData(null);

        CinemaImage saved = cinemaImageRepository.save(img);
        return cinemaImageMapper.cinemaImageToResponse(saved);
    }

    /**
     * Deletes the cinema's image if present. No-op if absent.
     */
    @Transactional
    public void delete(Long cinemaId) {
        cinemaImageRepository.findByCinema_Id(cinemaId)
                .ifPresent(cinemaImageRepository::delete);
    }

}
