package com.oraex.reconciliation.domain.port;

import com.oraex.reconciliation.domain.model.ProcessorPaymentRecord;

import java.util.List;
import java.util.Optional;

public interface ProcessorPaymentRecordPort {
    Optional<ProcessorPaymentRecord> findByPaymentId(String paymentId);
    List<ProcessorPaymentRecord> findAll();
}
