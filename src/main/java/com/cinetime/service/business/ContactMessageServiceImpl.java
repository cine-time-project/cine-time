package com.cinetime.service.business;

import com.cinetime.entity.business.ContactMessage;
import com.cinetime.payload.request.business.ContactMessageRequest;
import com.cinetime.repository.business.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository repo;

    public ContactMessageServiceImpl(ContactMessageRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public ContactMessage save(ContactMessageRequest req) {
        ContactMessage cm = new ContactMessage();
        cm.setFullName(req.getFullName());
        cm.setEmail(req.getEmail());
        cm.setPhoneNumber(req.getPhoneNumber());
        cm.setSubject(req.getSubject());
        cm.setMessage(req.getMessage());
        return repo.save(cm);
    }
}