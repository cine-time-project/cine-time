package com.cinetime.service.business;

import com.cinetime.entity.business.ContactMessage;
import com.cinetime.payload.request.business.ContactMessageRequest;
import com.cinetime.repository.business.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessage> getAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ContactMessage getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact message not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Contact message not found with id: " + id);
        }
        repo.deleteById(id);
    }
}
