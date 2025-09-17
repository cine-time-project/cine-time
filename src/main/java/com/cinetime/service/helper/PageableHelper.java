package com.cinetime.service.helper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageableHelper {

    // İsterseniz eski create(...) da kalsın; bu yeni imza MovieService'deki çağrıyla birebir uyumlu
    public Pageable buildPageable(int page, int size, String sort, String type) {
        int p = Math.max(0, page);
        int s = size <= 0 ? 10 : size;

        String sortProp = (sort == null || sort.isBlank()) ? "id" : sort;

        Sort.Direction dir;
        try {
            dir = Sort.Direction.fromString(type == null ? "ASC" : type);
        } catch (IllegalArgumentException ex) {
            dir = Sort.Direction.ASC;
        }

        return PageRequest.of(p, s, Sort.by(dir, sortProp));
    }
}
