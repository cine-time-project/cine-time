package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.Hall;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.HallMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.HallRequest;
import com.cinetime.payload.response.business.HallResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.HallRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final CinemaService cinemaService;
    private final HallMapper hallMapper;

    public ResponseMessage<HallResponse> saveHall(@Valid HallRequest hallRequest) {
        Cinema cinema = cinemaService.getById(hallRequest.getCinemaId());
        Hall hall = hallMapper.mapRequestToHall(hallRequest, cinema);
        Hall savedHall = hallRepository.save(hall);
        return ResponseMessage.<HallResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message(SuccessMessages.HALL_CREATED)
                .returnBody(hallMapper.mapHallToResponse(savedHall))
                .build();
    }

    private Hall findHallById(Long id) {
        return hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.HALL_NOT_FOUND_ID, id)));
    }

    public ResponseMessage<HallResponse> getHallById(Long hallId) {
        Hall hall = findHallById(hallId);
        return ResponseMessage.<HallResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.HALL_FOUND)
                .returnBody(hallMapper.mapHallToResponse(hall))
                .build();
    }
}
