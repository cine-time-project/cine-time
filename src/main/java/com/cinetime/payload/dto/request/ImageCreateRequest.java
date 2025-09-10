package com.cinetime.payload.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageCreateRequest {

    @NotNull
    private Long movieId;

    @NotNull
    private MultipartFile file;

    private boolean isPoster = false;
}