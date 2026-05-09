package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;

import java.util.Optional;

public interface ReconciliationRule {

    int priority();

    Optional<ReconciliationDifference> evaluate(ReconciliationContext context);
}
