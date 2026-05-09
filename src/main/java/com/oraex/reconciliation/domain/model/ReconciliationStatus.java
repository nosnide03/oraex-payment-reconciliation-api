package com.oraex.reconciliation.domain.model;

public enum ReconciliationStatus {
    RECONCILED,
    ONLY_INTERNAL,
    ONLY_PROCESSOR,
    NOT_FOUND,
    RECONCILED_WITH_DIFFERENCES
}
