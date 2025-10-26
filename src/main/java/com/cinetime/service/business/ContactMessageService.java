package com.cinetime.service.business;

import com.cinetime.entity.business.ContactMessage;
import com.cinetime.payload.request.business.ContactMessageRequest;

import java.util.List;

public interface ContactMessageService {
    ContactMessage save(ContactMessageRequest req);
    List<ContactMessage> getAll();
    ContactMessage getById(Long id);
    void deleteById(Long id);
}
