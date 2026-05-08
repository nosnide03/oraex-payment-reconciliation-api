package com.oraex.reconciliation.domain.port;

import com.oraex.reconciliation.domain.model.InternalPaymentRecord;

import java.util.List;
import java.util.Optional;

public interface InternalPaymentRecordPort {
    Optional<InternalPaymentRecord> findByPaymentId(String paymentId);
    List<InternalPaymentRecord> findAll();
}
