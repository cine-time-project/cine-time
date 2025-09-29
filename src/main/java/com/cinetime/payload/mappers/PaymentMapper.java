package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Payment;
import com.cinetime.entity.business.Ticket;
import com.cinetime.payload.response.business.PaymentResponse;
import com.cinetime.payload.response.business.TicketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentMapper {
    private final TicketMapper ticketMapper;

    public PaymentResponse mapPaymentToPaymentResponse(Payment payment, List<Ticket> tickets){
        List<TicketResponse> ticketDTOs = tickets.stream().map(ticketMapper::mapTicketToTicketResponse).toList();

        return PaymentResponse.builder()
                .tickets(ticketDTOs)
                .paymentId(payment.getId())
                .paymentAmount(payment.getAmount())
                .paymentIdempotencyKey(payment.getIdempotencyKey())
                .paymentCurrency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus())
                .paymentProviderReference(payment.getProviderReference())
                .paymentDate(payment.getPaymentDate())
                .build();

    }


}
