package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.InternalPaymentRecord;
import com.oraex.reconciliation.domain.model.ProcessorPaymentRecord;

import java.util.Optional;

public record ReconciliationContext(
        String paymentId,
        Optional<InternalPaymentRecord> internalPayment,
        Optional<ProcessorPaymentRecord> processorPayment
) {}
