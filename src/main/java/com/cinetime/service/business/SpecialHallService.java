package com.cinetime.service.business;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.SpecialHall;
import com.cinetime.payload.mappers.SpecialHallMapper;
import com.cinetime.payload.request.business.SpecialHallRequest;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.repository.business.HallRepository;
import com.cinetime.repository.business.SpecialHallRepository;
import com.cinetime.repository.business.SpecialHallTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class SpecialHallService {
    private final SpecialHallRepository repo;
    private final HallRepository hallRepo;
    private final SpecialHallTypeRepository typeRepo;

    public Page<SpecialHallResponse> list(Pageable pageable){
        return repo.findAll(pageable).map(SpecialHallMapper::toResponse);
    }

    public SpecialHallResponse get(Long id){
        var ent = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Special hall not found"));
        return SpecialHallMapper.toResponse(ent);
    }

    public SpecialHallResponse assign(SpecialHallRequest req){
        var hall = hallRepo.findById(req.getHallId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hall not found"));
        var type = typeRepo.findById(req.getTypeId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Type not found"));

        if (repo.existsByHall_Id(hall.getId())){
            throw new ResponseStatusException(CONFLICT, "This hall already has a special type");
        }

        var ent = SpecialHall.builder().hall(hall).type(type).build();
        return SpecialHallMapper.toResponse(repo.save(ent));
    }

    public SpecialHallResponse update(Long id, SpecialHallRequest req){
        var ent = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Special hall not found"));
        var hall = hallRepo.findById(req.getHallId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hall not found"));
        var type = typeRepo.findById(req.getTypeId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Type not found"));

        // başkası bu hall’e atanmış mı?
        var existsOnHall = repo.findByHall_Id(hall.getId())
                .filter(e -> !e.getId().equals(id)).isPresent();
        if (existsOnHall) throw new ResponseStatusException(CONFLICT, "This hall already has a special type");

        ent.setHall(hall);
        ent.setType(type);
        return SpecialHallMapper.toResponse(repo.save(ent));
    }

    public void delete(Long id){
        if (!repo.existsById(id)) throw new ResponseStatusException(NOT_FOUND, "Special hall not found");
        repo.deleteById(id);
    }
}