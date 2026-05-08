package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StatusMismatchRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 140;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return context.internalPayment()
                .flatMap(internal -> context.processorPayment()
                        .filter(processor -> internal.status() != processor.status())
                        .map(processor -> new ReconciliationDifference(
                                "status",
                                internal.status().name(),
                                processor.status().name(),
                                ReconciliationDifferenceType.STATUS_MISMATCH,
                                ReconciliationStatus.STATUS_MISMATCH
                        )));
    }
}
