package com.cinetime.service.impl;


import com.cinetime.entity.Image;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.dto.request.ImageCreateRequest;
import com.cinetime.payload.dto.response.ImageResponse;
import com.cinetime.repository.ImageRepository;
import com.cinetime.service.ImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    @Override
    @Transactional
    public ImageResponse uploadImage(ImageCreateRequest request) {
        try {
            if (request.isPoster() && imageRepository.existsByMovieIdAndIsPosterTrue(request.getMovieId())) {
                throw new ConflictException("This movie already has a poster");
            }

            Image image = new Image();
            image.setMovieId(request.getMovieId());
            image.setPoster(request.isPoster());
            image.setName(request.getFile().getOriginalFilename());
            image.setType(request.getFile().getContentType());
            image.setData(request.getFile().getBytes());

            Image saved = imageRepository.save(image);
            return mapToResponse(saved);
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
    }

    @Override
    public ImageResponse getImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));
        return mapToResponse(image);
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));
        imageRepository.delete(image);
    }

    @Override
    @Transactional
    public ImageResponse updateImage(Long id, ImageCreateRequest request) {
        try {
            Image image = imageRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));

            if (request.isPoster() && imageRepository.existsByMovieIdAndIsPosterTrue(request.getMovieId())
                    && !image.isPoster()) {
                throw new ConflictException("This movie already has a poster");
            }

            image.setName(request.getFile().getOriginalFilename());
            image.setType(request.getFile().getContentType());
            image.setData(request.getFile().getBytes());
            image.setPoster(request.isPoster());

            Image updated = imageRepository.save(image);
            return mapToResponse(updated);
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
    }

    @Override
    public List<ImageResponse> getImagesByMovie(Long movieId) {
        return imageRepository.findByMovieId(movieId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ImageResponse mapToResponse(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getName(),
                image.getType(),
                image.isPoster(),
                image.getMovieId(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }
}