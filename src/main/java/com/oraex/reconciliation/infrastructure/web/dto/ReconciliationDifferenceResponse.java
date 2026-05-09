package com.oraex.reconciliation.infrastructure.web.dto;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;

public record ReconciliationDifferenceResponse(
        String field,
        String internalValue,
        String processorValue,
        ReconciliationDifferenceType type
) {
    public static ReconciliationDifferenceResponse from(ReconciliationDifference difference) {
        return new ReconciliationDifferenceResponse(
                difference.field(),
                difference.internalValue(),
                difference.processorValue(),
                difference.type()
        );
    }
}
