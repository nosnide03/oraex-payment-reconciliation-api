package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotFoundRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return Optional.of(context)
                .filter(value -> value.internalPayment().isEmpty())
                .filter(value -> value.processorPayment().isEmpty())
                .map(value -> new ReconciliationDifference(
                        "payment",
                        "NOT_FOUND",
                        "NOT_FOUND",
                        ReconciliationDifferenceType.NOT_FOUND,
                        ReconciliationStatus.NOT_FOUND
                ));
    }
}
