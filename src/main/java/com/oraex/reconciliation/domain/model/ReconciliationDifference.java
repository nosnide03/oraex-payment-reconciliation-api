package com.oraex.reconciliation.domain.model;

public record ReconciliationDifference(
        String field,
        String internalValue,
        String processorValue,
        ReconciliationDifferenceType type
) {}
