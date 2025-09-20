package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Hall;
import com.cinetime.payload.response.business.SpecialHallResponse;
import org.springframework.stereotype.Component;

@Component
public class HallMapper {
    public SpecialHallResponse toSpecial(Hall hall){
        if(hall==null) return null;
        var cinema=hall.getCinema();
        return SpecialHallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .seatCapacity(hall.getSeatCapacity())
                .cinemaId(cinema!=null?cinema.getId():null)
                .cinemaName(cinema!=null?cinema.getName():null)
                .build();
    }
}
