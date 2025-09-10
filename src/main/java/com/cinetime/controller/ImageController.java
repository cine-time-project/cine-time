package com.cinetime.controller;

import com.cinetime.payload.dto.request.ImageCreateRequest;
import com.cinetime.payload.dto.response.ImageResponse;
import com.cinetime.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getImage(@PathVariable Long id) {
        return ResponseEntity.ok(imageService.getImage(id));
    }

    @PostMapping
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam Long movieId,
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isPoster) {

        ImageCreateRequest request = new ImageCreateRequest();
        request.setMovieId(movieId);
        request.setFile(file);
        request.setPoster(isPoster);

        ImageResponse response = imageService.uploadImage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageResponse> updateImage(
            @PathVariable Long id,
            @RequestParam Long movieId,
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isPoster) {

        ImageCreateRequest request = new ImageCreateRequest();
        request.setMovieId(movieId);
        request.setFile(file);
        request.setPoster(isPoster);

        ImageResponse response = imageService.updateImage(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ImageResponse>> getImagesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(imageService.getImagesByMovie(movieId));
    }
}