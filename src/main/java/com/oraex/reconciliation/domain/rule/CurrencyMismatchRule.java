package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class CurrencyMismatchRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 130;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return context.internalPayment()
                .flatMap(internal -> context.processorPayment()
                        .filter(processor -> !Objects.equals(internal.currency(), processor.currency()))
                        .map(processor -> new ReconciliationDifference(
                                "currency",
                                internal.currency(),
                                processor.currency(),
                                ReconciliationDifferenceType.CURRENCY_MISMATCH,
                                ReconciliationStatus.RECONCILED_WITH_DIFFERENCES
                        )));
    }
}
