package com.oraex.reconciliation.infrastructure.web;

import com.oraex.reconciliation.application.usecase.ReconcilePaymentUseCase;
import com.oraex.reconciliation.application.usecase.SearchPaymentReconciliationsUseCase;
import com.oraex.reconciliation.domain.model.ReconciliationStatus;
import com.oraex.reconciliation.infrastructure.web.dto.PaymentReconciliationResponse;
import com.oraex.reconciliation.infrastructure.web.mapper.PaymentReconciliationResponseMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/v1/reconciliations/payments")
public class ReconciliationController {

    private final ReconcilePaymentUseCase reconcilePaymentUseCase;
    private final SearchPaymentReconciliationsUseCase searchPaymentReconciliationsUseCase;
    private final PaymentReconciliationResponseMapper mapper;

    public ReconciliationController(
            ReconcilePaymentUseCase reconcilePaymentUseCase,
            SearchPaymentReconciliationsUseCase searchPaymentReconciliationsUseCase,
            PaymentReconciliationResponseMapper mapper
    ) {
        this.reconcilePaymentUseCase = reconcilePaymentUseCase;
        this.searchPaymentReconciliationsUseCase = searchPaymentReconciliationsUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{paymentId}")
    public PaymentReconciliationResponse reconcileByPaymentId(
            @PathVariable @NotBlank String paymentId
    ) {
        return mapper.toResponse(reconcilePaymentUseCase.execute(paymentId));
    }

    @GetMapping
    public List<PaymentReconciliationResponse> search(
            @RequestParam Optional<String> merchantId,
            @RequestParam Optional<ReconciliationStatus> reconciliationStatus,
            @RequestParam Optional<Boolean> reconciled
    ) {
        return searchPaymentReconciliationsUseCase.execute(merchantId, reconciliationStatus, reconciled)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
