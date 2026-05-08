package com.oraex.reconciliation.application.usecase;

import com.oraex.reconciliation.domain.model.PaymentReconciliationResult;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import com.oraex.reconciliation.domain.port.InternalPaymentRecordPort;
import com.oraex.reconciliation.domain.port.ProcessorPaymentRecordPort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SearchPaymentReconciliationsUseCase {

    private final ReconcilePaymentUseCase reconcilePaymentUseCase;
    private final InternalPaymentRecordPort internalPaymentRecordPort;
    private final ProcessorPaymentRecordPort processorPaymentRecordPort;

    public SearchPaymentReconciliationsUseCase(
            ReconcilePaymentUseCase reconcilePaymentUseCase,
            InternalPaymentRecordPort internalPaymentRecordPort,
            ProcessorPaymentRecordPort processorPaymentRecordPort
    ) {
        this.reconcilePaymentUseCase = reconcilePaymentUseCase;
        this.internalPaymentRecordPort = internalPaymentRecordPort;
        this.processorPaymentRecordPort = processorPaymentRecordPort;
    }

    public List<PaymentReconciliationResult> execute(
            Optional<String> merchantId,
            Optional<ReconciliationStatus> reconciliationStatus,
            Optional<Boolean> reconciled
    ) {
        Set<String> paymentIds = new LinkedHashSet<>();
        internalPaymentRecordPort.findAll().forEach(record -> paymentIds.add(record.paymentId()));
        processorPaymentRecordPort.findAll().forEach(record -> paymentIds.add(record.paymentId()));

        return paymentIds.stream()
                .map(reconcilePaymentUseCase::execute)
                .filter(result -> merchantId.map(value -> value.equals(result.merchantId())).orElse(true))
                .filter(result -> reconciliationStatus.map(value -> value == result.reconciliationStatus()).orElse(true))
                .filter(result -> reconciled.map(value -> value == result.reconciled()).orElse(true))
                .toList();
    }
}
