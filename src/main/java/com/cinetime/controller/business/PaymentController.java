package com.cinetime.controller.business;


import com.cinetime.payload.request.business.PaymentFilter;
import com.cinetime.payload.response.business.PaymentResponse;

import com.cinetime.service.business.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
private final PaymentService paymentService;

    @PreAuthorize("hasAnyAuthority('EMPLOYEE','ADMIN')")
    @GetMapping("/allpayments")
    public ResponseEntity< Page<PaymentResponse>> allPayments(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "0") Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "ASC") String type) {

           Page<PaymentResponse> paymentResponses= paymentService.findAllPayments(page, size, sort, type);

         return   ResponseEntity.ok(paymentResponses);

    }


    @PreAuthorize("hasAnyAuthority('EMPLOYEE','ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> listPayments(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "DESC") String type,

            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String providerRef,
            @RequestParam(required = false) String idempotencyKey,
            @RequestParam(required = false) String q
    ) {
        var filter = new PaymentFilter();
        filter.setUserId(userId);
        filter.setEmail(email);
        filter.setPhone(phone);
        filter.setStatus(status);
        filter.setFrom(from);
        filter.setTo(to);
        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);
        filter.setProviderRef(providerRef);
        filter.setIdempotencyKey(idempotencyKey);
        filter.setQ(q);

        Page<PaymentResponse> pageResp = paymentService.findPayments(page, size, sort, type, filter);
        return ResponseEntity.ok(pageResp);
    }

}
