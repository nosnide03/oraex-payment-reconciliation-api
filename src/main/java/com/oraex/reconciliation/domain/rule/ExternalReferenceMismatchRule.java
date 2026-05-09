package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class ExternalReferenceMismatchRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 110;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return context.internalPayment()
                .flatMap(internal -> context.processorPayment()
                        .filter(processor -> !Objects.equals(internal.externalReference(), processor.externalReference()))
                        .map(processor -> new ReconciliationDifference(
                                "externalReference",
                                String.valueOf(internal.externalReference()),
                                processor.externalReference(),
                                ReconciliationDifferenceType.REFERENCE_MISMATCH,
                                ReconciliationStatus.RECONCILED_WITH_DIFFERENCES
                        )));
    }
}
