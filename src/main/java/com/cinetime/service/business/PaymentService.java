package com.cinetime.service.business;

import com.cinetime.entity.business.Payment;
import com.cinetime.entity.enums.PaymentStatus;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.PaymentMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.request.business.PaymentFilter;
import com.cinetime.payload.response.business.PaymentResponse;
import com.cinetime.payload.response.user.UserPaymentSummaryResponse;
import com.cinetime.repository.business.PaymentRepository;
import com.cinetime.service.helper.PageableHelper;
import com.cinetime.service.helper.PaymentSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final PageableHelper pageableHelper;



    @Transactional(readOnly = true)
    public Page<PaymentResponse> findAllPayments(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);
        return paymentRepository.findAllWithTickets(pageable)
                .map(paymentMapper::mapPaymentToPaymentResponse); // NEVER return null here
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> findPayments(
            Integer page, Integer size, String sort, String type,
            PaymentFilter filter
    ) {
        Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);
        var spec = PaymentSpecifications.withFilters(filter);
        Page<Payment> payments = paymentRepository.findAll(spec, pageable);
        return payments.map(paymentMapper::mapPaymentToPaymentResponse);
    }


    // in some UserService or PaymentService (your choice; I’ll keep it in PaymentService)
    @Transactional(readOnly = true)
    public UserPaymentSummaryResponse getUserPaymentSummary(Long userId) {
        var status = PaymentStatus.SUCCESS; // use the enum value you consider as a successful/settled payment
        var total = paymentRepository.sumByUserAndStatus(userId, status);
        var count = paymentRepository.countByUser_IdAndPaymentStatus(userId, status);
        var last  = paymentRepository.lastAtByUserAndStatus(userId, status);
        return new UserPaymentSummaryResponse(total, count, last);
    }

    @Transactional
    public void deletePaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PAYMENT_NOT_FOUND));
        // CascadeType.ALL on Payment.tickets will remove tickets automatically
        paymentRepository.delete(payment);
    }

    @Transactional
    public void deletePaymentForUser(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PAYMENT_NOT_FOUND));

        if (!payment.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(ErrorMessages.ACCESS_DANIED);
        }
        paymentRepository.delete(payment);
    }
}


