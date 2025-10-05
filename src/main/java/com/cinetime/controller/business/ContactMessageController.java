package com.cinetime.controller.business;

import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.ContactMessageRequest;
import com.cinetime.payload.response.business.ApiMessageResponse;
import com.cinetime.service.business.ContactMessageService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contactmessages")
public class ContactMessageController {

    private final ContactMessageService service;

    public ContactMessageController(ContactMessageService service) {
        this.service = service;
    }

    @PermitAll
    @PostMapping
    public ResponseEntity<ApiMessageResponse> create(@Valid @RequestBody ContactMessageRequest req) {
        service.save(req);
        return ResponseEntity.ok(
                new ApiMessageResponse(SuccessMessages.MESSAGE_TAKEN)
        );
    }
}