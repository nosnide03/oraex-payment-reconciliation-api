package com.oraex.reconciliation.infrastructure.web.dto;

import com.oraex.reconciliation.domain.model.ReconciliationStatus;

import java.util.List;

public record PaymentReconciliationResponse(
        String paymentId,
        String merchantId,
        boolean reconciled,
        ReconciliationStatus reconciliationStatus,
        PaymentRecordResponse internalPayment,
        PaymentRecordResponse processorPayment,
        List<ReconciliationDifferenceResponse> differences,
        String message
) {}
