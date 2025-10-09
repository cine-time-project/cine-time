package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.CinemaImage;
import com.cinetime.entity.business.Image;
import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaImageMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.CinemaImageResponse;
import com.cinetime.payload.response.business.ImageResponse;
import com.cinetime.repository.business.CinemaImageRepository;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.service.validator.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(ErrorMessages.CINEMA_NOT_FOUND, cinemaId)));

        if (cinema.getCinemaImage() != null) {
            throw new ConflictException("Cinema already has an image");
        }

        CinemaImage cinemaImage;
        try {
            cinemaImage = CinemaImage.builder()
                    .cinema(cinema)
                    .name(file.getOriginalFilename())
                    .type(file.getContentType())
                    .data(file.getBytes())
                    .build();
        } catch (java.io.IOException e) {
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
                    .build();

            CinemaImage saved = cinemaImageRepository.save(cinemaImage);
            return cinemaImageMapper.cinemaImageToResponse(saved);
        } catch (Exception e) {
            throw new RuntimeException("Could not download image from url", e);
        }
    }

}
