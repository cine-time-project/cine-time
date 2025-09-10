package com.cinetime.service;


import com.cinetime.payload.dto.request.ImageCreateRequest;
import com.cinetime.payload.dto.response.ImageResponse;

import java.util.List;

public interface ImageService {
    ImageResponse uploadImage(ImageCreateRequest request);
    ImageResponse getImage(Long id);
    void deleteImage(Long id);
    ImageResponse updateImage(Long id, ImageCreateRequest request);
    List<ImageResponse> getImagesByMovie(Long movieId);
}