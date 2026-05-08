package com.oraex.reconciliation.domain.model;

public enum ReconciliationDifferenceType {
    ONLY_INTERNAL,
    ONLY_PROCESSOR,
    AMOUNT_MISMATCH,
    CURRENCY_MISMATCH,
    STATUS_MISMATCH,
    MERCHANT_MISMATCH,
    REFERENCE_MISMATCH,
    NOT_FOUND
}
