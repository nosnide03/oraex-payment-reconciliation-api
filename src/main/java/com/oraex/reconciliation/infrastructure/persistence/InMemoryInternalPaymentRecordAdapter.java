package com.oraex.reconciliation.infrastructure.persistence;

import com.oraex.reconciliation.domain.model.InternalPaymentRecord;
import com.oraex.reconciliation.domain.model.PaymentStatus;
import com.oraex.reconciliation.domain.port.InternalPaymentRecordPort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryInternalPaymentRecordAdapter implements InternalPaymentRecordPort {

    private final Map<String, InternalPaymentRecord> records = new LinkedHashMap<>();

    public InMemoryInternalPaymentRecordAdapter() {
        add(new InternalPaymentRecord("PAY-1001", "MERCHANT-001", "EXT-9001", bd("10.00"), "USD", PaymentStatus.PAID, date("2026-05-07T10:00:00")));
        add(new InternalPaymentRecord("PAY-1002", "MERCHANT-001", "EXT-9002", bd("15.00"), "USD", PaymentStatus.PAID, date("2026-05-07T10:10:00")));
        add(new InternalPaymentRecord("PAY-1004", "MERCHANT-001", "EXT-9004", bd("10.00"), "USD", PaymentStatus.PAID, date("2026-05-07T10:20:00")));
        add(new InternalPaymentRecord("PAY-1005", "MERCHANT-001", "EXT-9005", bd("12.00"), "USD", PaymentStatus.PAID, date("2026-05-07T10:30:00")));
        add(new InternalPaymentRecord("PAY-1006", "MERCHANT-001", "EXT-9006", bd("20.00"), "USD", PaymentStatus.PAID, date("2026-05-07T10:40:00")));
        add(new InternalPaymentRecord("PAY-1007", "MERCHANT-001", "EXT-9007", bd("30.00"), "USD", PaymentStatus.PAID, date("2026-05-07T10:50:00")));
        add(new InternalPaymentRecord("PAY-1008", "MERCHANT-001", "EXT-9008", bd("40.00"), "USD", PaymentStatus.PAID, date("2026-05-07T11:00:00")));
    }

    @Override
    public Optional<InternalPaymentRecord> findByPaymentId(String paymentId) {
        return Optional.ofNullable(records.get(paymentId));
    }

    @Override
    public List<InternalPaymentRecord> findAll() {
        return List.copyOf(records.values());
    }

    private void add(InternalPaymentRecord record) {
        records.put(record.paymentId(), record);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private LocalDateTime date(String value) {
        return LocalDateTime.parse(value);
    }
}
