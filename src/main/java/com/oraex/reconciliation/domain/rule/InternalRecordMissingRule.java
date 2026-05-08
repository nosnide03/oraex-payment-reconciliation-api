package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InternalRecordMissingRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return context.processorPayment()
                .filter(processor -> context.internalPayment().isEmpty())
                .map(processor -> new ReconciliationDifference(
                        "payment",
                        "NOT_FOUND",
                        "FOUND",
                        ReconciliationDifferenceType.ONLY_PROCESSOR,
                        ReconciliationStatus.ONLY_PROCESSOR
                ));
    }
}
