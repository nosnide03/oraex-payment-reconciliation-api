package com.oraex.reconciliation.domain.service;

import com.oraex.reconciliation.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentReconciliationServiceTest {

    private final PaymentReconciliationService service = new PaymentReconciliationService();

    @Test
    void shouldReturnReconciledWhenBothRecordsMatch() {
        InternalPaymentRecord internal = internal("PAY-1001", "MERCHANT-001", "EXT-9001", "10.00", "USD", PaymentStatus.PAID);
        ProcessorPaymentRecord processor = processor("PAY-1001", "MERCHANT-001", "EXT-9001", "10.00", "USD", PaymentStatus.PAID);

        PaymentReconciliationResult result = service.reconcile("PAY-1001", Optional.of(internal), Optional.of(processor));

        assertThat(result.reconciled()).isTrue();
        assertThat(result.reconciliationStatus()).isEqualTo(ReconciliationStatus.RECONCILED);
        assertThat(result.differences()).isEmpty();
    }

    @Test
    void shouldReturnOnlyInternalWhenProcessorRecordDoesNotExist() {
        InternalPaymentRecord internal = internal("PAY-1002", "MERCHANT-001", "EXT-9002", "15.00", "USD", PaymentStatus.PAID);

        PaymentReconciliationResult result = service.reconcile("PAY-1002", Optional.of(internal), Optional.empty());

        assertThat(result.reconciled()).isFalse();
        assertThat(result.reconciliationStatus()).isEqualTo(ReconciliationStatus.ONLY_INTERNAL);
    }

    @Test
    void shouldReturnOnlyProcessorWhenInternalRecordDoesNotExist() {
        ProcessorPaymentRecord processor = processor("PAY-1003", "MERCHANT-001", "EXT-9003", "20.00", "USD", PaymentStatus.PAID);

        PaymentReconciliationResult result = service.reconcile("PAY-1003", Optional.empty(), Optional.of(processor));

        assertThat(result.reconciled()).isFalse();
        assertThat(result.reconciliationStatus()).isEqualTo(ReconciliationStatus.ONLY_PROCESSOR);
    }

    @Test
    void shouldReturnAmountMismatchWhenAmountsAreDifferent() {
        InternalPaymentRecord internal = internal("PAY-1004", "MERCHANT-001", "EXT-9004", "10.00", "USD", PaymentStatus.PAID);
        ProcessorPaymentRecord processor = processor("PAY-1004", "MERCHANT-001", "EXT-9004", "9.99", "USD", PaymentStatus.PAID);

        PaymentReconciliationResult result = service.reconcile("PAY-1004", Optional.of(internal), Optional.of(processor));

        assertThat(result.reconciled()).isFalse();
        assertThat(result.reconciliationStatus()).isEqualTo(ReconciliationStatus.AMOUNT_MISMATCH);
        assertThat(result.differences()).extracting(ReconciliationDifference::type)
                .containsExactly(ReconciliationDifferenceType.AMOUNT_MISMATCH);
    }

    @Test
    void shouldReturnCurrencyMismatchWhenCurrenciesAreDifferent() {
        InternalPaymentRecord internal = internal("PAY-1005", "MERCHANT-001", "EXT-9005", "12.00", "USD", PaymentStatus.PAID);
        ProcessorPaymentRecord processor = processor("PAY-1005", "MERCHANT-001", "EXT-9005", "12.00", "EUR", PaymentStatus.PAID);

        PaymentReconciliationResult result = service.reconcile("PAY-1005", Optional.of(internal), Optional.of(processor));

        assertThat(result.reconciliationStatus()).isEqualTo(ReconciliationStatus.CURRENCY_MISMATCH);
    }

    @Test
    void shouldReturnStatusMismatchWhenStatusesAreDifferent() {
        InternalPaymentRecord internal = internal("PAY-1006", "MERCHANT-001", "EXT-9006", "20.00", "USD", PaymentStatus.PAID);
        ProcessorPaymentRecord processor = processor("PAY-1006", "MERCHANT-001", "EXT-9006", "20.00", "USD", PaymentStatus.REVERSED);

        PaymentReconciliationResult result = service.reconcile("PAY-1006", Optional.of(internal), Optional.of(processor));

        assertThat(result.reconciliationStatus()).isEqualTo(ReconciliationStatus.STATUS_MISMATCH);
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExistInAnySource() {
        PaymentReconciliationResult result = service.reconcile("PAY-9999", Optional.empty(), Optional.empty());

        assertThat(result.reconciled()).isFalse();
        assertThat(result.reconciliationStatus()).isEqualTo(ReconciliationStatus.NOT_FOUND);
    }

    private InternalPaymentRecord internal(String paymentId, String merchantId, String externalReference, String amount, String currency, PaymentStatus status) {
        return new InternalPaymentRecord(paymentId, merchantId, externalReference, new BigDecimal(amount), currency, status, LocalDateTime.parse("2026-05-07T10:00:00"));
    }

    private ProcessorPaymentRecord processor(String paymentId, String merchantId, String externalReference, String amount, String currency, PaymentStatus status) {
        return new ProcessorPaymentRecord(paymentId, merchantId, externalReference, new BigDecimal(amount), currency, status, LocalDateTime.parse("2026-05-08T02:00:00"));
    }
}
