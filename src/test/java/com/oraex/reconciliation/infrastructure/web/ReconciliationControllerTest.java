package com.oraex.reconciliation.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReconciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnReconciledPayment() throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/payments/PAY-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is("PAY-1001")))
                .andExpect(jsonPath("$.reconciled", is(true)))
                .andExpect(jsonPath("$.reconciliationStatus", is("RECONCILED")))
                .andExpect(jsonPath("$.differences.length()", is(0)));
    }

    @Test
    void shouldReturnAmountMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/payments/PAY-1004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconciled", is(false)))
                .andExpect(jsonPath("$.reconciliationStatus", is("RECONCILED_WITH_DIFFERENCES")))
                .andExpect(jsonPath("$.differences[0].type", is("AMOUNT_MISMATCH")))
                .andExpect(jsonPath("$.differences[0].field", is("amount")));
    }

    @Test
    void shouldReturnMerchantMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/payments/PAY-1007"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconciled", is(false)))
                .andExpect(jsonPath("$.reconciliationStatus", is("RECONCILED_WITH_DIFFERENCES")))
                .andExpect(jsonPath("$.differences[0].type", is("MERCHANT_MISMATCH")))
                .andExpect(jsonPath("$.differences[0].field", is("merchantId")));
    }

    @Test
    void shouldReturnReferenceMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/payments/PAY-1008"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconciled", is(false)))
                .andExpect(jsonPath("$.reconciliationStatus", is("RECONCILED_WITH_DIFFERENCES")))
                .andExpect(jsonPath("$.differences[0].type", is("REFERENCE_MISMATCH")))
                .andExpect(jsonPath("$.differences[0].field", is("externalReference")));
    }

    @Test
    void shouldReturnFilteredUnreconciledPayments() throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/payments?reconciled=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reconciled", is(false)));
    }

    @Test
    void shouldReturnFilteredPaymentsByControlledReconciliationStatus() throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/payments?reconciliationStatus=RECONCILED_WITH_DIFFERENCES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reconciled", is(false)))
                .andExpect(jsonPath("$[0].reconciliationStatus", is("RECONCILED_WITH_DIFFERENCES")));
    }
}
