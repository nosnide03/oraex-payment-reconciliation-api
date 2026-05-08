package com.oraex.reconciliation.infrastructure.persistence;

import com.oraex.reconciliation.domain.model.PaymentStatus;
import com.oraex.reconciliation.domain.model.ProcessorPaymentRecord;
import com.oraex.reconciliation.domain.port.ProcessorPaymentRecordPort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryProcessorPaymentRecordAdapter implements ProcessorPaymentRecordPort {

    private final Map<String, ProcessorPaymentRecord> records = new LinkedHashMap<>();

    public InMemoryProcessorPaymentRecordAdapter() {
        add(new ProcessorPaymentRecord("PAY-1001", "MERCHANT-001", "EXT-9001", bd("10.00"), "USD", PaymentStatus.PAID, date("2026-05-08T02:00:00")));
        add(new ProcessorPaymentRecord("PAY-1003", "MERCHANT-001", "EXT-9003", bd("20.00"), "USD", PaymentStatus.PAID, date("2026-05-08T02:05:00")));
        add(new ProcessorPaymentRecord("PAY-1004", "MERCHANT-001", "EXT-9004", bd("9.99"), "USD", PaymentStatus.PAID, date("2026-05-08T02:10:00")));
        add(new ProcessorPaymentRecord("PAY-1005", "MERCHANT-001", "EXT-9005", bd("12.00"), "EUR", PaymentStatus.PAID, date("2026-05-08T02:15:00")));
        add(new ProcessorPaymentRecord("PAY-1006", "MERCHANT-001", "EXT-9006", bd("20.00"), "USD", PaymentStatus.REVERSED, date("2026-05-08T02:20:00")));
        add(new ProcessorPaymentRecord("PAY-1007", "MERCHANT-999", "EXT-9007", bd("30.00"), "USD", PaymentStatus.PAID, date("2026-05-08T02:25:00")));
        add(new ProcessorPaymentRecord("PAY-1008", "MERCHANT-001", "EXT-9999", bd("40.00"), "USD", PaymentStatus.PAID, date("2026-05-08T02:30:00")));
    }

    @Override
    public Optional<ProcessorPaymentRecord> findByPaymentId(String paymentId) {
        return Optional.ofNullable(records.get(paymentId));
    }

    @Override
    public List<ProcessorPaymentRecord> findAll() {
        return List.copyOf(records.values());
    }

    private void add(ProcessorPaymentRecord record) {
        records.put(record.paymentId(), record);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private LocalDateTime date(String value) {
        return LocalDateTime.parse(value);
    }
}
