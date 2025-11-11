package com.cinetime.payload.mappers;

import com.cinetime.entity.business.SpecialHallType;
import com.cinetime.payload.request.business.SpecialHallTypeRequest;
import com.cinetime.payload.response.business.SpecialHallTypeResponse;

public final class SpecialHallTypeMapper {
    private SpecialHallTypeMapper(){}

    public static SpecialHallType toEntity(SpecialHallTypeRequest req){
        return SpecialHallType.builder()
                .name(req.getName().trim())
                .priceDiffPercent(req.getPriceDiffPercent())
                .build();
    }

    public static void update(SpecialHallType ent, SpecialHallTypeRequest req){
        ent.setName(req.getName().trim());
        ent.setPriceDiffPercent(req.getPriceDiffPercent());
    }

    public static SpecialHallTypeResponse toResponse(SpecialHallType ent){
        return SpecialHallTypeResponse.builder()
                .id(ent.getId())
                .name(ent.getName())
                .priceDiffPercent(ent.getPriceDiffPercent())
                .build();
    }
}
