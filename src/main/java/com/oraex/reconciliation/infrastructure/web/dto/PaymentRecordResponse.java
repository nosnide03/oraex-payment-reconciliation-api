package com.oraex.reconciliation.infrastructure.web.dto;

import com.oraex.reconciliation.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRecordResponse(
        boolean exists,
        String paymentId,
        String externalReference,
        String merchantId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        LocalDateTime processedAt,
        LocalDateTime reportedAt
) {
    public static PaymentRecordResponse notFound() {
        return new PaymentRecordResponse(false, null, null, null, null, null, null, null, null);
    }
}
