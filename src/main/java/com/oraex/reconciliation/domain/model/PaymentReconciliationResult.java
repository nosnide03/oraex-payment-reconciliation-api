package com.oraex.reconciliation.domain.model;

import com.oraex.reconciliation.domain.rule.ReconciliationContext;

import java.util.List;
import java.util.Optional;

public record PaymentReconciliationResult(
        String paymentId,
        String merchantId,
        boolean reconciled,
        ReconciliationStatus reconciliationStatus,
        Optional<InternalPaymentRecord> internalPayment,
        Optional<ProcessorPaymentRecord> processorPayment,
        List<ReconciliationDifference> differences
) {

    public static PaymentReconciliationResult from(ReconciliationContext context, List<ReconciliationDifference> differences) {
        List<ReconciliationDifference> safeDifferences = List.copyOf(differences);
        return new PaymentReconciliationResult(
                context.paymentId(),
                resolveMerchantId(context),
                safeDifferences.isEmpty(),
                resolveStatus(safeDifferences),
                context.internalPayment(),
                context.processorPayment(),
                safeDifferences
        );
    }

    private static ReconciliationStatus resolveStatus(List<ReconciliationDifference> differences) {
        return differences.stream()
                .map(ReconciliationDifference::resultingStatus)
                .findFirst()
                .orElse(ReconciliationStatus.RECONCILED);
    }

    private static String resolveMerchantId(ReconciliationContext context) {
        return context.internalPayment()
                .map(InternalPaymentRecord::merchantId)
                .or(() -> context.processorPayment().map(ProcessorPaymentRecord::merchantId))
                .orElse(null);
    }
}
