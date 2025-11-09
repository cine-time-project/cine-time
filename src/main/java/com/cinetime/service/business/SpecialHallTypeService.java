package com.cinetime.service.business;


import com.cinetime.payload.mappers.SpecialHallTypeMapper;
import com.cinetime.payload.request.business.SpecialHallTypeRequest;
import com.cinetime.payload.response.business.SpecialHallTypeResponse;
import com.cinetime.repository.business.SpecialHallTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class SpecialHallTypeService {
    private final SpecialHallTypeRepository repo;

    public Page<SpecialHallTypeResponse> list(Pageable pageable){
        return repo.findAll(pageable).map(SpecialHallTypeMapper::toResponse);
    }

    public SpecialHallTypeResponse get(Long id){
        var ent = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Type not found"));
        return SpecialHallTypeMapper.toResponse(ent);
    }

    public SpecialHallTypeResponse create(SpecialHallTypeRequest req){
        validateNameUnique(req.getName(), null);
        var saved = repo.save(SpecialHallTypeMapper.toEntity(req));
        return SpecialHallTypeMapper.toResponse(saved);
    }

    public SpecialHallTypeResponse update(Long id, SpecialHallTypeRequest req){
        var ent = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Type not found"));
        validateNameUnique(req.getName(), id);
        SpecialHallTypeMapper.update(ent, req);
        return SpecialHallTypeMapper.toResponse(repo.save(ent));
    }

    public void delete(Long id){
        if (!repo.existsById(id)) throw new ResponseStatusException(NOT_FOUND, "Type not found");
        repo.deleteById(id);
    }

    private void validateNameUnique(String name, Long currentId){
        var ex = repo.findByNameIgnoreCase(name.trim());
        if (ex.isPresent() && (currentId == null || !Objects.equals(ex.get().getId(), currentId))){
            throw new ResponseStatusException(CONFLICT, "Type name already exists");
        }
    }
}