package com.oraex.reconciliation.infrastructure.web.mapper;

import com.oraex.reconciliation.domain.model.InternalPaymentRecord;
import com.oraex.reconciliation.domain.model.PaymentReconciliationResult;
import com.oraex.reconciliation.domain.model.ProcessorPaymentRecord;
import com.oraex.reconciliation.infrastructure.web.dto.PaymentReconciliationResponse;
import com.oraex.reconciliation.infrastructure.web.dto.PaymentRecordResponse;
import com.oraex.reconciliation.infrastructure.web.dto.ReconciliationDifferenceResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentReconciliationResponseMapper {

    public PaymentReconciliationResponse toResponse(PaymentReconciliationResult result) {
        return new PaymentReconciliationResponse(
                result.paymentId(),
                result.merchantId(),
                result.reconciled(),
                result.reconciliationStatus(),
                result.internalPayment().map(this::toInternalResponse).orElse(PaymentRecordResponse.notFound()),
                result.processorPayment().map(this::toProcessorResponse).orElse(PaymentRecordResponse.notFound()),
                result.differences().stream()
                        .map(difference -> new ReconciliationDifferenceResponse(
                                difference.field(),
                                difference.internalValue(),
                                difference.processorValue(),
                                difference.type()
                        ))
                        .toList(),
                result.message()
        );
    }

    private PaymentRecordResponse toInternalResponse(InternalPaymentRecord record) {
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

    private PaymentRecordResponse toProcessorResponse(ProcessorPaymentRecord record) {
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
