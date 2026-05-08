package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AmountMismatchRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 120;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return context.internalPayment()
                .flatMap(internal -> context.processorPayment()
                        .filter(processor -> internal.amount().compareTo(processor.amount()) != 0)
                        .map(processor -> new ReconciliationDifference(
                                "amount",
                                internal.amount().toPlainString(),
                                processor.amount().toPlainString(),
                                ReconciliationDifferenceType.AMOUNT_MISMATCH,
                                ReconciliationStatus.AMOUNT_MISMATCH
                        )));
    }
}
