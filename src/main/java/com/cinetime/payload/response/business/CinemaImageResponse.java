package com.cinetime.payload.response.business;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CinemaImageResponse {
    private Long id;
    private String name;
    private String type;     //
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; private
    Long cinemaId;
    private String url;

}
