package com.oraex.reconciliation.application.usecase;

import com.oraex.reconciliation.domain.model.PaymentReconciliationResult;
import com.oraex.reconciliation.domain.port.InternalPaymentRecordPort;
import com.oraex.reconciliation.domain.port.ProcessorPaymentRecordPort;
import com.oraex.reconciliation.domain.service.PaymentReconciliationService;
import org.springframework.stereotype.Service;

@Service
public class ReconcilePaymentUseCase {

    private final InternalPaymentRecordPort internalPaymentRecordPort;
    private final ProcessorPaymentRecordPort processorPaymentRecordPort;
    private final PaymentReconciliationService reconciliationService;

    public ReconcilePaymentUseCase(
            InternalPaymentRecordPort internalPaymentRecordPort,
            ProcessorPaymentRecordPort processorPaymentRecordPort,
            PaymentReconciliationService reconciliationService
    ) {
        this.internalPaymentRecordPort = internalPaymentRecordPort;
        this.processorPaymentRecordPort = processorPaymentRecordPort;
        this.reconciliationService = reconciliationService;
    }

    public PaymentReconciliationResult execute(String paymentId) {
        return reconciliationService.reconcile(
                paymentId,
                internalPaymentRecordPort.findByPaymentId(paymentId).orElse(null),
                processorPaymentRecordPort.findByPaymentId(paymentId).orElse(null)
        );
    }
}
