package com.cinetime.payload.mappers;


import com.cinetime.entity.business.Image;
import com.cinetime.payload.response.business.ImageResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Component
public class ImageMapper {

    public ImageResponse toResponse(Image img) {
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(String.valueOf(img.getId()))
                .toUriString();

        return ImageResponse.builder()
                .id(img.getId())
                .name(img.getName())
                .type(img.getType())
                .poster(img.isPoster())
                .movieId(img.getMovie() == null ? null : img.getMovie().getId())
                .createdAt(img.getCreatedAt())
                .updatedAt(img.getUpdatedAt())
                .url(url)
                .build();
    }
}