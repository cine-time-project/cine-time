package com.cinetime.service.validator;

import com.cinetime.exception.BadRequestException;
import com.cinetime.payload.messages.ErrorMessages;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

public final class ImageValidator {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_MIME = Set.of(
            MediaType.IMAGE_JPEG_VALUE,   // image/jpeg
            MediaType.IMAGE_PNG_VALUE,    // image/png
            "image/webp"                  // image/webp
    );

    private ImageValidator() {}

    /** Validate presence, size and allowed content type. */
    public static void requireValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorMessages.FILE_MUST_NOT_BE_EMPTY);
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException(ErrorMessages.FILE_SIZE_EXCEEDED);
        }
        String type = normalizeMime(file.getContentType());
        if (type == null || !ALLOWED_MIME.contains(type)) {
            throw new BadRequestException(String.format(ErrorMessages.UNSUPPORTED_CONTENT_TYPE, type));
        }
    }

    /** Normalize common aliases (e.g., image/jpg → image/jpeg). */
    private static String normalizeMime(String ct) {
        if (ct == null || ct.isBlank()) return null;
        String lower = ct.toLowerCase(Locale.ROOT).trim();
        if ("image/jpg".equals(lower)) return MediaType.IMAGE_JPEG_VALUE;
        return lower;
    }

    /** Strip path parts, replace unsafe chars, and clamp to 200 chars. */
    public static String cleanFileName(String original) {
        if (original == null) return "image";
        String base = original.replace("\\", "/");
        base = base.substring(base.lastIndexOf('/') + 1);
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.length() > 200) base = base.substring(base.length() - 200);
        return base;
    }

    /** Read bytes and wrap IO issues into a BadRequest. */
    public static byte[] safeBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new BadRequestException("could not read uploaded file: " + e.getMessage());
        }
    }
}