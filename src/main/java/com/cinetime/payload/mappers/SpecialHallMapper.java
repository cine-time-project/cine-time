package com.cinetime.payload.mappers;

import com.cinetime.entity.business.SpecialHall;
import com.cinetime.payload.response.business.SpecialHallResponse;

public final class SpecialHallMapper {
    private SpecialHallMapper(){}

    public static SpecialHallResponse toResponse(SpecialHall ent){
        var hall = ent.getHall();
        var cinema = hall.getCinema();
        return SpecialHallResponse.builder()
                .id(ent.getId())
                .name(ent.getType().getName())           // response’taki name = tip adı
                .seatCapacity(hall.getSeatCapacity())     // Hall’dan
                .cinemaId(cinema.getId())
                .cinemaName(cinema.getName())
                .build();
    }
}