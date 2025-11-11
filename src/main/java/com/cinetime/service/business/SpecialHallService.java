package com.cinetime.service.business;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.SpecialHall;
import com.cinetime.entity.business.SpecialHallType;
import com.cinetime.payload.mappers.SpecialHallMapper;
import com.cinetime.payload.request.business.SpecialHallRequest;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.repository.business.HallRepository;
import com.cinetime.repository.business.SpecialHallRepository;
import com.cinetime.repository.business.SpecialHallTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpecialHallService {

    private final SpecialHallRepository repo;
    private final HallRepository hallRepo;
    private final SpecialHallTypeRepository typeRepo;

    /* =========================
       LIST
       ========================= */
    @Transactional(readOnly = true)
    public Page<SpecialHallResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(SpecialHallMapper::toResponse);
    }

    /* =========================
       GET
       ========================= */
    @Transactional(readOnly = true)
    public SpecialHallResponse get(Long id) {
        SpecialHall sh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Special hall not found: " + id));
        return SpecialHallMapper.toResponse(sh);
    }

    /* =========================
       CREATE/ASSIGN (upsert)
       - Hall.isSpecial = true  (sadece kısmi update)
       ========================= */
    @Transactional
    public SpecialHallResponse assign(SpecialHallRequest req) {
        Hall hall = hallRepo.findById(req.getHallId())
                .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + req.getHallId()));

        SpecialHallType type = typeRepo.findById(req.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Type not found: " + req.getTypeId()));

        // aynı hall'e ikinci bir satır açma (unique)
        SpecialHall sh = repo.findByHallId(hall.getId())
                .orElseGet(() -> SpecialHall.builder().hall(hall).build());

        sh.setType(type);
        repo.save(sh);

        // yalnızca isSpecial kolonunu güncelle (name gibi alanlara dokunma)
        if (Boolean.FALSE.equals(hall.getIsSpecial())) {
            hallRepo.updateIsSpecialById(hall.getId(), true);
        }

        return SpecialHallMapper.toResponse(sh);
    }

    /* =========================
       UPDATE
       - Tip değişirse: sadece SH.type
       - Hall değişirse: eski hall isSpecial=false, yeni hall isSpecial=true (kısmi update)
       ========================= */
    @Transactional
    public SpecialHallResponse update(Long id, SpecialHallRequest req) {
        SpecialHall sh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Special hall not found: " + id));

        SpecialHallType newType = typeRepo.findById(req.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Type not found: " + req.getTypeId()));

        Long currentHallId = sh.getHall().getId();
        Long requestedHallId = req.getHallId();

        if (!currentHallId.equals(requestedHallId)) {
            // Eski hall sıradan
            hallRepo.updateIsSpecialById(currentHallId, false);

            // Yeni hall özel
            Hall newHall = hallRepo.findById(requestedHallId)
                    .orElseThrow(() -> new IllegalArgumentException("Hall not found: " + requestedHallId));
            hallRepo.updateIsSpecialById(newHall.getId(), true);

            sh.setHall(newHall);
        }

        sh.setType(newType);
        repo.save(sh);

        return SpecialHallMapper.toResponse(sh);
    }

    /* =========================
       DELETE
       - Hall.isSpecial=false (kısmi update)
       ========================= */
    @Transactional
    public void delete(Long id) {
        SpecialHall sh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Special hall not found: " + id));

        Long hallId = sh.getHall().getId();
        repo.delete(sh);

        // yalnızca isSpecial indir
        hallRepo.updateIsSpecialById(hallId, false);
    }
}
