package com.cinetime.payload.mappers;

import com.cinetime.entity.business.*;
import com.cinetime.payload.response.business.SpecialHallResponse;

public final class SpecialHallMapper {
    private SpecialHallMapper() {}

    public static SpecialHallResponse toResponse(SpecialHall sh) {
        Hall h = sh.getHall();
        SpecialHallType t = sh.getType();

        return SpecialHallResponse.builder()
                .id(sh.getId())
                .hallId(h.getId())
                .hallName(h.getName())
                .seatCapacity(h.getSeatCapacity())
                .cinemaId(h.getCinema().getId())
                .cinemaName(h.getCinema().getName())
                .typeId(t.getId())
                .typeName(t.getName())
                .priceDiffPercent(t.getPriceDiffPercent())
                .build();
    }
}