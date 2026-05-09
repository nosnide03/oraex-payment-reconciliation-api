package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProcessorRecordMissingRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return context.internalPayment()
                .filter(internal -> context.processorPayment().isEmpty())
                .map(internal -> new ReconciliationDifference(
                        "payment",
                        "FOUND",
                        "NOT_FOUND",
                        ReconciliationDifferenceType.ONLY_INTERNAL,
                        ReconciliationStatus.ONLY_INTERNAL
                ));
    }
}
