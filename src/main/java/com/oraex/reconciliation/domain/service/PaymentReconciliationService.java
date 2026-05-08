package com.oraex.reconciliation.domain.service;

import com.oraex.reconciliation.domain.model.InternalPaymentRecord;
import com.oraex.reconciliation.domain.model.PaymentReconciliationResult;
import com.oraex.reconciliation.domain.model.ProcessorPaymentRecord;
import com.oraex.reconciliation.domain.model.ReconciliationDifference;
import com.oraex.reconciliation.domain.rule.ReconciliationContext;
import com.oraex.reconciliation.domain.rule.ReconciliationRule;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentReconciliationService {

    private final List<ReconciliationRule> rules;

    public PaymentReconciliationService(List<ReconciliationRule> rules) {
        this.rules = rules;
    }

    public PaymentReconciliationResult reconcile(
            String paymentId,
            InternalPaymentRecord internalPayment,
            ProcessorPaymentRecord processorPayment
    ) {
        ReconciliationContext context = new ReconciliationContext(
                paymentId,
                Optional.ofNullable(internalPayment),
                Optional.ofNullable(processorPayment)
        );
        return PaymentReconciliationResult.from(context, evaluateRules(context));
    }

    private List<ReconciliationDifference> evaluateRules(ReconciliationContext context) {
        return rules.stream()
                .sorted(Comparator.comparingInt(ReconciliationRule::priority))
                .map(rule -> rule.evaluate(context))
                .flatMap(Optional::stream)
                .toList();
    }
}
