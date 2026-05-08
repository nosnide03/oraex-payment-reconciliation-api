package com.oraex.reconciliation.domain.model;

public enum ReconciliationStatus {
    RECONCILED,
    ONLY_INTERNAL,
    ONLY_PROCESSOR,
    AMOUNT_MISMATCH,
    CURRENCY_MISMATCH,
    STATUS_MISMATCH,
    MERCHANT_MISMATCH,
    REFERENCE_MISMATCH,
    MULTIPLE_MISMATCHES,
    NOT_FOUND
}
