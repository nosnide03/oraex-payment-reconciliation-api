package com.oraex.reconciliation.domain.rule;

import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.model.ReconciliationDifferenceType;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class MerchantMismatchRule implements ReconciliationRule {

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public Optional<ReconciliationDifference> evaluate(ReconciliationContext context) {
        return context.internalPayment()
                .flatMap(internal -> context.processorPayment()
                        .filter(processor -> !Objects.equals(internal.merchantId(), processor.merchantId()))
                        .map(processor -> new ReconciliationDifference(
                                "merchantId",
                                internal.merchantId(),
                                processor.merchantId(),
                                ReconciliationDifferenceType.MERCHANT_MISMATCH,
                                ReconciliationStatus.RECONCILED_WITH_DIFFERENCES
                        )));
    }
}
