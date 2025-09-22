package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.Hall;
import com.cinetime.payload.request.business.HallRequest;
import com.cinetime.payload.response.business.HallResponse;
import com.cinetime.payload.response.business.SpecialHallResponse;
import org.springframework.data.domain.Page;
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

    public Hall mapRequestToHall(HallRequest request, Cinema cinema) {
        return Hall.builder()
                .name(request.getName())
                .seatCapacity(request.getSeatCapacity())
                .isSpecial(request.getIsSpecial() != null ? request.getIsSpecial() : false)
                .cinema(cinema)
                .build();
    }

    public HallResponse mapHallToResponse(Hall hall) {
        return HallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .seatCapacity(hall.getSeatCapacity())
                .isSpecial(hall.getIsSpecial())
                .createdAt(hall.getCreatedAt())
                .updatedAt(hall.getUpdatedAt())
                .cinemaId(hall.getCinema() != null ? hall.getCinema().getId() : null)
                .cinemaName(hall.getCinema() != null ? hall.getCinema().getName() : null)
                .build();
    }

    public Page<HallResponse> mapToResponsePage(Page<Hall> halls) {
        return halls.map(this::mapHallToResponse);
    }

}
