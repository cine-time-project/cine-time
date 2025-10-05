package com.cinetime.service.business;


import com.cinetime.entity.business.ContactMessage;
import com.cinetime.payload.request.business.ContactMessageRequest;

public interface ContactMessageService {
    ContactMessage save(ContactMessageRequest req);
}