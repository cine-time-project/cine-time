package com.cinetime.payload.response.business;

import com.cinetime.entity.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class PaymentResponse {

    @Builder.Default
    private List<TicketResponse> tickets = List.of();

    private Long paymentId;
    private Double paymentAmount;
    private String paymentIdempotencyKey;
    private String paymentProviderReference;
    private String paymentCurrency;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}