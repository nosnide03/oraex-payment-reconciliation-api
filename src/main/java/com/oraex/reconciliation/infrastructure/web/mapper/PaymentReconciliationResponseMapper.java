package com.oraex.reconciliation.infrastructure.web.mapper;

import com.oraex.reconciliation.domain.model.PaymentReconciliationResult;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import com.oraex.reconciliation.infrastructure.web.dto.PaymentReconciliationResponse;
import com.oraex.reconciliation.infrastructure.web.dto.PaymentRecordResponse;
import com.oraex.reconciliation.infrastructure.web.dto.ReconciliationDifferenceResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentReconciliationResponseMapper {

    public PaymentReconciliationResponse toResponse(PaymentReconciliationResult result) {
        return new PaymentReconciliationResponse(
                result.paymentId(),
                result.merchantId(),
                result.reconciled(),
                result.reconciliationStatus(),
                mapInternalPayment(result),
                mapProcessorPayment(result),
                mapDifferences(result),
                resolveMessage(result.reconciliationStatus())
        );
    }

    private PaymentRecordResponse mapInternalPayment(PaymentReconciliationResult result) {
        return result.internalPayment()
                .map(PaymentRecordResponse::fromInternal)
                .orElseGet(PaymentRecordResponse::notFound);
    }

    private PaymentRecordResponse mapProcessorPayment(PaymentReconciliationResult result) {
        return result.processorPayment()
                .map(PaymentRecordResponse::fromProcessor)
                .orElseGet(PaymentRecordResponse::notFound);
    }

    private List<ReconciliationDifferenceResponse> mapDifferences(PaymentReconciliationResult result) {
        return result.differences().stream()
                .map(ReconciliationDifferenceResponse::from)
                .toList();
    }

    private String resolveMessage(ReconciliationStatus status) {
        return switch (status) {
            case RECONCILED -> "Payment is reconciled successfully";
            case NOT_FOUND -> "Payment was not found in internal records or processor records";
            case ONLY_INTERNAL -> "Payment exists internally but was not reported by the processor";
            case ONLY_PROCESSOR -> "Payment was reported by the processor but does not exist internally";
            case RECONCILED_WITH_DIFFERENCES -> "Payment exists in both sources but contains reconciliation differences";
        };
    }
}
