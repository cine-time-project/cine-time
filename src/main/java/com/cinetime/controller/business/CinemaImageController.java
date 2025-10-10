package com.cinetime.controller.business;

import com.cinetime.entity.business.CinemaImage;
import com.cinetime.entity.business.Image;
import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.CinemaImageResponse;
import com.cinetime.payload.response.business.ImageResponse;
import com.cinetime.service.business.CinemaImageService;
import com.cinetime.service.validator.ImageValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Tag(name = "CinemaImages")
@RestController
@RequestMapping("/api/cinemaimages")
@RequiredArgsConstructor
public class CinemaImageController {

    private final CinemaImageService cinemaImageService;

    @PreAuthorize("permitAll()")
    @GetMapping("/{cinemaId}")
    public ResponseEntity<byte[]> getCinemaImage(@PathVariable Long cinemaId) {
        CinemaImage img = cinemaImageService.getCinemaImage(cinemaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                img.getType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : img.getType()));
        headers.setContentDispositionFormData("inline", img.getName());
        headers.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        headers.add("X-Success-Message", SuccessMessages.IMAGE_FETCHED);
        return new ResponseEntity<>(img.getData(), headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Upload an image for a cinema",
            responses = {@ApiResponse(responseCode = "201", description = "Created")})
    @PostMapping(path = "/{cinemaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CinemaImageResponse> upload(@PathVariable Long cinemaId,
                                                      @RequestParam("file") MultipartFile file)
                                                        {
               CinemaImageResponse created = cinemaImageService.upload(cinemaId, file );
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, created.getUrl())
                .header("X-Success-Message", SuccessMessages.IMAGE_UPLOADED)
                .body(created);
    }



    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Upload an image for a cinema from a remote URL",
            responses = {@ApiResponse(responseCode = "201", description = "Created")})
    @PostMapping(path = "/{cinemaId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CinemaImageResponse> uploadByUrl(@PathVariable Long cinemaId,
                                                           @RequestBody java.util.Map<String, String> body) {
        String url = body == null ? null : body.get("url");
        CinemaImageResponse created = cinemaImageService.uploadFromUrl(cinemaId, url);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, created.getUrl())
                .header("X-Success-Message", SuccessMessages.IMAGE_UPLOADED)
                .body(created);
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(path = "/{cinemaId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CinemaImageResponse> replaceByUrl(
            @PathVariable Long cinemaId,
            @RequestBody java.util.Map<String, String> body) {

        String url = body == null ? null : body.get("url");
        CinemaImageResponse updated = cinemaImageService.replaceWithUrl(cinemaId, url);
        return ResponseEntity.ok()
                .header("X-Success-Message", SuccessMessages.IMAGE_UPLOADED)
                .body(updated);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{cinemaId}")
    public ResponseEntity<Void> deleteCinemaImage(@PathVariable Long cinemaId) {
        cinemaImageService.delete(cinemaId);
        return ResponseEntity.noContent().build();
    }

}
