package com.cinetime.payload.request.business;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFilter {
    private Long userId; //filter by user
    private String email; // filter by email (optional)
    private String phone; // filter by phone(optional)
    private String status; // PAID, REFUNDED

    @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
    private LocalDate from; // paymentDate >=from.atStartOfDay()

    @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
    private LocalDate to; // paymentDate < to.PlusDays(1).atStartOfDay()

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private String providerRef;
    private String idempotencyKey;
    private String q;









}
