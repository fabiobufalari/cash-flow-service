package com.bufalari.cashflow.client;

import com.bufalari.cashflow.dto.PayableSummaryDTO; // DTO neste serviço, já com ID UUID
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "accounts-payable-client-cf", url = "${payable.service.url}")
public interface AccountsPayableClient {

    /**
     * Fetches paid payable summaries based on payment transaction dates.
     * Assumes the corresponding endpoint exists in AccountsPayableService and returns a List<PayableSummaryDTO> with UUID IDs.
     * Busca resumos de contas a pagar pagas com base nas datas das transações de pagamento.
     * Assume que o endpoint correspondente existe no AccountsPayableService e retorna List<PayableSummaryDTO> com IDs UUID.
     */
    // <<< PATH CORRIGIDO para corresponder ao Controller do AP >>>
    @GetMapping("/api/payables/summary-by-payment-date") // Path do Controller do AP
    List<PayableSummaryDTO> getPayablesSummaryByPaymentDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    /**
     * Fetches pending payable summaries based on due dates.
     * Assumes the corresponding endpoint exists in AccountsPayableService and returns a List<PayableSummaryDTO> with UUID IDs.
     * Busca resumos de contas a pagar pendentes com base nas datas de vencimento.
     * Assume que o endpoint correspondente existe no AccountsPayableService e retorna List<PayableSummaryDTO> com IDs UUID.
     */
    // <<< PATH CORRIGIDO para corresponder ao Controller do AP >>>
    @GetMapping("/api/payables/pending-summary-by-due-date") // Path do Controller do AP
    List<PayableSummaryDTO> getPendingPayablesSummaryByDueDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}