package com.oraex.reconciliation.infrastructure.web.dto;

import com.oraex.reconciliation.domain.model.InternalPaymentRecord;
import com.oraex.reconciliation.domain.model.PaymentStatus;
import com.oraex.reconciliation.domain.model.ProcessorPaymentRecord;

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
        return new PaymentRecordResponse(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static PaymentRecordResponse fromInternal(InternalPaymentRecord record) {
        return new PaymentRecordResponse(
                true,
                record.paymentId(),
                record.externalReference(),
                record.merchantId(),
                record.amount(),
                record.currency(),
                record.status(),
                record.processedAt(),
                null
        );
    }

    public static PaymentRecordResponse fromProcessor(ProcessorPaymentRecord record) {
        return new PaymentRecordResponse(
                true,
                record.paymentId(),
                record.externalReference(),
                record.merchantId(),
                record.amount(),
                record.currency(),
                record.status(),
                null,
                record.reportedAt()
        );
    }
}
