package com.cinetime.payload.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserPaymentSummaryResponse {
    private BigDecimal totalSpent;      // sum of PAID amounts
    private long paymentsCount;         // count of PAID payments
    private LocalDateTime lastPaymentDate; // max paymentDate for PAID
}
