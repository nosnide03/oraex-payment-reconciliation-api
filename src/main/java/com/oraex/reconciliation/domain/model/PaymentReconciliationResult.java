package com.oraex.reconciliation.domain.model;

import java.util.List;
import java.util.Optional;

public record PaymentReconciliationResult(
        String paymentId,
        String merchantId,
        boolean reconciled,
        ReconciliationStatus reconciliationStatus,
        Optional<InternalPaymentRecord> internalPayment,
        Optional<ProcessorPaymentRecord> processorPayment,
        List<ReconciliationDifference> differences,
        String message
) {}
