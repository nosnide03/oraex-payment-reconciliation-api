package com.oraex.reconciliation.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record ProcessorPaymentRecord(
        String paymentId,
        String merchantId,
        String externalReference,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        LocalDateTime reportedAt
) {
    public ProcessorPaymentRecord {
        Objects.requireNonNull(paymentId, "paymentId is required");
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(externalReference, "externalReference is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        Objects.requireNonNull(status, "status is required");
    }
}
