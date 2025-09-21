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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final CinemaService cinemaService;
    private final HallMapper hallMapper;

    @Transactional
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

    public Hall findHallById(Long id) {
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

    public ResponseMessage<Page<HallResponse>> getAllHalls(Pageable pageable) {
        Page<Hall> halls = hallRepository.findAll(pageable);
        if (halls.isEmpty()) throw new ResourceNotFoundException(ErrorMessages.HALLS_NOT_FOUND);
        return ResponseMessage.<Page<HallResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.HALLS_FOUND)
                .returnBody(hallMapper.mapToResponsePage(halls))
                .build();
    }

    public ResponseMessage<HallResponse> deleteHallById(Long hallId) {
        Hall hall = findHallById(hallId);
        HallResponse hallResponse = hallMapper.mapHallToResponse(hall);
        hallRepository.delete(hall);
        return ResponseMessage.<HallResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.HALL_DELETED)
                .returnBody(hallResponse)
                .build();
    }

    @Transactional
    public ResponseMessage<HallResponse> updateHall(@Valid HallRequest hallRequest, Long id) {

        Hall hall = findHallById(id);

        // 2️⃣ Update primitive fields if present in request
        if (hallRequest.getName() != null) {
            hall.setName(hallRequest.getName());
        }
        if (hallRequest.getSeatCapacity() != null) {
            hall.setSeatCapacity(hallRequest.getSeatCapacity());
        }
        if (hallRequest.getIsSpecial() != null) {
            hall.setIsSpecial(hallRequest.getIsSpecial());
        }
        if (hallRequest.getCinemaId() != null) {
            hall.setCinema(cinemaService.getById(hallRequest.getCinemaId()));
        }

        // 3️⃣ Save updated hall
        Hall updatedHall = hallRepository.save(hall);

        return ResponseMessage.<HallResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.HALL_UPDATED)
                .returnBody(hallMapper.mapHallToResponse(updatedHall))
                .build();
    }

}
