package com.cinetime.payload.mappers;

import com.cinetime.entity.business.City;
import com.cinetime.entity.business.District;
import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.payload.response.business.DistrictMiniResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class DistrictMapper {


    public DistrictMiniResponse districtToDistrictMiniResponse(District district){
        if (district==null) return null;
        return DistrictMiniResponse.builder()
                .name(district.getName())
                .id(district.getId())
                .build();
    }



}
