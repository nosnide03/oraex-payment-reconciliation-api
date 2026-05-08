package com.oraex.reconciliation.domain.service;

import com.oraex.reconciliation.domain.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentReconciliationService {

    public PaymentReconciliationResult reconcile(
            String paymentId,
            Optional<InternalPaymentRecord> internalPayment,
            Optional<ProcessorPaymentRecord> processorPayment
    ) {
        if (internalPayment.isEmpty() && processorPayment.isEmpty()) {
            return new PaymentReconciliationResult(
                    paymentId,
                    null,
                    false,
                    ReconciliationStatus.NOT_FOUND,
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new ReconciliationDifference(
                            "payment",
                            "NOT_FOUND",
                            "NOT_FOUND",
                            ReconciliationDifferenceType.NOT_FOUND
                    )),
                    "Payment was not found in internal records or processor records"
            );
        }

        if (internalPayment.isPresent() && processorPayment.isEmpty()) {
            InternalPaymentRecord internal = internalPayment.get();
            return new PaymentReconciliationResult(
                    paymentId,
                    internal.merchantId(),
                    false,
                    ReconciliationStatus.ONLY_INTERNAL,
                    internalPayment,
                    Optional.empty(),
                    List.of(new ReconciliationDifference(
                            "payment",
                            "FOUND",
                            "NOT_FOUND",
                            ReconciliationDifferenceType.ONLY_INTERNAL
                    )),
                    "Payment exists internally but was not reported by the processor"
            );
        }

        if (internalPayment.isEmpty()) {
            ProcessorPaymentRecord processor = processorPayment.get();
            return new PaymentReconciliationResult(
                    paymentId,
                    processor.merchantId(),
                    false,
                    ReconciliationStatus.ONLY_PROCESSOR,
                    Optional.empty(),
                    processorPayment,
                    List.of(new ReconciliationDifference(
                            "payment",
                            "NOT_FOUND",
                            "FOUND",
                            ReconciliationDifferenceType.ONLY_PROCESSOR
                    )),
                    "Payment was reported by the processor but does not exist internally"
            );
        }

        InternalPaymentRecord internal = internalPayment.get();
        ProcessorPaymentRecord processor = processorPayment.get();
        List<ReconciliationDifference> differences = findDifferences(internal, processor);
        boolean reconciled = differences.isEmpty();
        ReconciliationStatus status = reconciled ? ReconciliationStatus.RECONCILED : resolveStatus(differences);

        return new PaymentReconciliationResult(
                paymentId,
                internal.merchantId(),
                reconciled,
                status,
                internalPayment,
                processorPayment,
                differences,
                reconciled
                        ? "Payment is reconciled successfully"
                        : "Payment exists in both sources but contains reconciliation differences"
        );
    }

    private List<ReconciliationDifference> findDifferences(InternalPaymentRecord internal, ProcessorPaymentRecord processor) {
        List<ReconciliationDifference> differences = new ArrayList<>();

        if (!internal.merchantId().equals(processor.merchantId())) {
            differences.add(new ReconciliationDifference(
                    "merchantId",
                    internal.merchantId(),
                    processor.merchantId(),
                    ReconciliationDifferenceType.MERCHANT_MISMATCH
            ));
        }

        if (!safeEquals(internal.externalReference(), processor.externalReference())) {
            differences.add(new ReconciliationDifference(
                    "externalReference",
                    String.valueOf(internal.externalReference()),
                    processor.externalReference(),
                    ReconciliationDifferenceType.REFERENCE_MISMATCH
            ));
        }

        if (internal.amount().compareTo(processor.amount()) != 0) {
            differences.add(new ReconciliationDifference(
                    "amount",
                    internal.amount().toPlainString(),
                    processor.amount().toPlainString(),
                    ReconciliationDifferenceType.AMOUNT_MISMATCH
            ));
        }

        if (!internal.currency().equals(processor.currency())) {
            differences.add(new ReconciliationDifference(
                    "currency",
                    internal.currency(),
                    processor.currency(),
                    ReconciliationDifferenceType.CURRENCY_MISMATCH
            ));
        }

        if (internal.status() != processor.status()) {
            differences.add(new ReconciliationDifference(
                    "status",
                    internal.status().name(),
                    processor.status().name(),
                    ReconciliationDifferenceType.STATUS_MISMATCH
            ));
        }

        return differences;
    }

    private ReconciliationStatus resolveStatus(List<ReconciliationDifference> differences) {
        if (differences.size() > 1) {
            return ReconciliationStatus.MULTIPLE_MISMATCHES;
        }

        return switch (differences.get(0).type()) {
            case AMOUNT_MISMATCH -> ReconciliationStatus.AMOUNT_MISMATCH;
            case CURRENCY_MISMATCH -> ReconciliationStatus.CURRENCY_MISMATCH;
            case STATUS_MISMATCH -> ReconciliationStatus.STATUS_MISMATCH;
            case MERCHANT_MISMATCH -> ReconciliationStatus.MERCHANT_MISMATCH;
            case REFERENCE_MISMATCH -> ReconciliationStatus.REFERENCE_MISMATCH;
            case ONLY_INTERNAL -> ReconciliationStatus.ONLY_INTERNAL;
            case ONLY_PROCESSOR -> ReconciliationStatus.ONLY_PROCESSOR;
            case NOT_FOUND -> ReconciliationStatus.NOT_FOUND;
        };
    }

    private boolean safeEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
