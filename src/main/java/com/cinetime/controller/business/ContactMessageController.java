package com.cinetime.controller.business;

import com.cinetime.entity.business.ContactMessage;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.ContactMessageRequest;
import com.cinetime.payload.response.business.ApiMessageResponse;
import com.cinetime.service.business.ContactMessageService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contactmessages")
public class ContactMessageController {

    private final ContactMessageService service;

    public ContactMessageController(ContactMessageService service) {
        this.service = service;
    }

    // 🔹 Herkes erişebilir (iletişim formu)
    @PermitAll
    @PostMapping
    public ResponseEntity<ApiMessageResponse> create(@Valid @RequestBody ContactMessageRequest req) {
        service.save(req);
        return ResponseEntity.ok(
                new ApiMessageResponse(SuccessMessages.MESSAGE_TAKEN)
        );
    }

    // 🔹 Tüm mesajları getir (Sadece Admin veya Employee)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // 🔹 ID’ye göre mesaj getir (Sadece Admin veya Employee)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<ContactMessage> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // 🔹 Mesaj sil (Sadece Admin veya Employee)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok(
                new ApiMessageResponse(SuccessMessages.MESSAGE_DELETED)
        );
    }
}
