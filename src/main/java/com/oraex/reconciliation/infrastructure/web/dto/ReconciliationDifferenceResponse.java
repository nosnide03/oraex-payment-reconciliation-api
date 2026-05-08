package com.oraex.reconciliation.infrastructure.web.dto;

import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;

public record ReconciliationDifferenceResponse(
        String field,
        String internalValue,
        String processorValue,
        ReconciliationDifferenceType type
) {}
