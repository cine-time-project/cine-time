package com.cinetime.service.helper;

import com.cinetime.entity.business.Payment;
import com.cinetime.entity.enums.PaymentStatus;
import com.cinetime.payload.request.business.PaymentFilter;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class PaymentSpecifications {

    private PaymentSpecifications() {}

    public static Specification<Payment> withFilters(PaymentFilter f) {
        if (f == null) {
            return Specification.allOf(); // return an empty spec instead of null
        }
        return Specification.allOf(
                userIdEquals(f),
                emailLike(f),
                phoneLike(f),
                statusEquals(f),
                dateBetween(f),
                amountGte(f),
                amountLte(f),
                providerRefLike(f),
                idempotencyLike(f),
                freeText(f)
        );
    }

    private static Specification<Payment> userIdEquals(PaymentFilter f) {
        return (r, q, cb) -> (f.getUserId() == null)
                ? cb.conjunction()
                : cb.equal(r.get("user").get("id"), f.getUserId());
    }

    private static Specification<Payment> emailLike(PaymentFilter f) {
        return (r, q, cb) -> (f.getEmail() == null || f.getEmail().isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(r.get("user").get("email")), "%" + f.getEmail().trim().toLowerCase() + "%");
    }

    private static Specification<Payment> phoneLike(PaymentFilter f) {
        return (r, q, cb) -> (f.getPhone() == null || f.getPhone().isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(r.get("user").get("phoneNumber")), "%" + f.getPhone().trim().toLowerCase() + "%");
    }

    private static Specification<Payment> statusEquals(PaymentFilter f) {
        return (r, q, cb) -> {
            if (f.getStatus() == null || f.getStatus().isBlank()) return cb.conjunction();
            PaymentStatus ps;
            try {
                ps = PaymentStatus.valueOf(f.getStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return cb.disjunction(); // invalid status -> no results
            }
            return cb.equal(r.get("paymentStatus"), ps);
        };
    }

    private static Specification<Payment> dateBetween(PaymentFilter f) {
        return (r, q, cb) -> {
            if (f.getFrom() == null && f.getTo() == null) return cb.conjunction();
            LocalDateTime from = (f.getFrom() == null) ? null : f.getFrom().atStartOfDay();
            LocalDateTime to = (f.getTo() == null) ? null : f.getTo().plusDays(1).atStartOfDay();
            if (from != null && to != null && from.isAfter(to)) {
                LocalDateTime tmp = from; from = to; to = tmp;
            }
            if (from != null && to != null) return cb.between(r.get("paymentDate"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(r.get("paymentDate"), from);
            return cb.lessThan(r.get("paymentDate"), to);
        };
    }

    private static Specification<Payment> amountGte(PaymentFilter f) {
        return (r, q, cb) -> f.getMinAmount() == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(r.get("amount"), f.getMinAmount());
    }

    private static Specification<Payment> amountLte(PaymentFilter f) {
        return (r, q, cb) -> f.getMaxAmount() == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(r.get("amount"), f.getMaxAmount());
    }

    private static Specification<Payment> providerRefLike(PaymentFilter f) {
        return (r, q, cb) -> (f.getProviderRef() == null || f.getProviderRef().isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(r.get("providerReference")), "%" + f.getProviderRef().trim().toLowerCase() + "%");
    }

    private static Specification<Payment> idempotencyLike(PaymentFilter f) {
        return (r, q, cb) -> (f.getIdempotencyKey() == null || f.getIdempotencyKey().isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(r.get("idempotencyKey")), "%" + f.getIdempotencyKey().trim().toLowerCase() + "%");
    }

    /** free-text across email/phone/providerRef/idempotencyKey */
    private static Specification<Payment> freeText(PaymentFilter f) {
        return (r, q, cb) -> {
            if (f.getQ() == null || f.getQ().isBlank()) return cb.conjunction();
            String like = "%" + f.getQ().trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(r.get("user").get("email")), like),
                    cb.like(cb.lower(r.get("user").get("phoneNumber")), like),
                    cb.like(cb.lower(r.get("providerReference")), like),
                    cb.like(cb.lower(r.get("idempotencyKey")), like)
            );
        };
    }
}