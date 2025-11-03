package com.cinetime.controller.business;

import com.cinetime.entity.business.Image;
import com.cinetime.payload.mappers.ImageMapper;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.ImageResponse;
import com.cinetime.service.business.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "Images")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    private final ImageMapper imageMapper;

    /**
     * I01 — Stream image bytes by id (sets content type and long cache).
     * Publicly accessible.
     */
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get image bytes by id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Image bytes",
                            content = @Content(mediaType = "image/*")),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    @GetMapping("/images/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        var img = imageService.getImageEntity(imageId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                img.getType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : img.getType()));
        headers.setContentDispositionFormData("inline", img.getName());
        headers.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        headers.add("X-Success-Message", SuccessMessages.IMAGE_FETCHED);
        return new ResponseEntity<>(img.getData(), headers, HttpStatus.OK);
    }

    /**
     * I02 — Upload image for a movie; use poster=true to mark as poster.
     * Requires ADMIN authority.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Upload an image for a movie",
            responses = {@ApiResponse(responseCode = "201", description = "Created")})
    @PostMapping(path = "/images/{movieId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> upload(@PathVariable Long movieId,
                                                @RequestParam("file") MultipartFile file,
                                                @RequestParam(name = "poster", defaultValue = "false") boolean poster) {
        ImageResponse created = imageService.upload(movieId, file, poster);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, created.getUrl())
                .header("X-Success-Message", SuccessMessages.IMAGE_UPLOADED)
                .body(created);
    }

    /**
     * I03 — Delete image by id.
     * Requires ADMIN authority.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete an image",
            responses = {@ApiResponse(responseCode = "204", description = "No Content")})
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable Long imageId) {
        imageService.delete(imageId);
        return ResponseEntity.noContent()
                .header("X-Success-Message", SuccessMessages.IMAGE_DELETED)
                .build();
    }

    /**
     * I04 — Replace image bytes and optionally toggle poster flag.
     * Requires ADMIN authority.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update an image",
            description = "Replaces image bytes and optionally toggles poster flag.",
            responses = {@ApiResponse(responseCode = "200", description = "OK")})
    @PutMapping(path = "/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> update(@PathVariable Long imageId,
                                                @RequestParam("file") MultipartFile file,
                                                @RequestParam(name = "poster", required = false) Boolean poster) {
        ImageResponse updated = imageService.update(imageId, file, poster);
        return ResponseEntity.ok()
                .header("X-Success-Message", SuccessMessages.IMAGE_UPDATED)
                .body(updated);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/movies/{movieId}/images")
    public ResponseEntity<List<Image>> getImagesForMovie(@PathVariable Long movieId) {
        List<Image> images = imageService.findImagesByMovieId(movieId);
        return ResponseEntity.ok(images);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/movies/{movieId}/poster")
    public ResponseEntity<Long> getPosterImageId(@PathVariable Long movieId) {
        Long posterId = imageService.findPosterIdByMovieId(movieId);
        return ResponseEntity.ok(posterId);
    }

    /**
     * Dashboard endpoint - Get paginated list of all images
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard/images/list")
    public ResponseEntity<?> getAllImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long movieId
    ) {
        var images = imageService.getAllImagesPaginated(page, size, sort, type, q, movieId);

        var result = new java.util.HashMap<String, Object>();
        result.put("httpStatus", "OK");
        result.put("message", "Images retrieved successfully");
        result.put("returnBody", images);

        return ResponseEntity.ok(result);
    }

    /**
     * Dashboard endpoint - Get single image details
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard/images/{imageId}")
    public ResponseEntity<?> getImageDetails(@PathVariable Long imageId) {
        var image = imageService.getImageEntity(imageId);
        var response = imageMapper.toResponse(image);

        var result = new java.util.HashMap<String, Object>();
        result.put("httpStatus", "OK");
        result.put("message", "Image retrieved successfully");
        result.put("returnBody", response);

        return ResponseEntity.ok(result);
    }


}
