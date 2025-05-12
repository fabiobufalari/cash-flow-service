package com.bufalari.cashflow.client;

import com.bufalari.cashflow.dto.ReceivableSummaryDTO; // DTO neste serviço, já com ID UUID
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "accounts-receivable-client-cf", url = "${receivable.service.url}")
public interface AccountsReceivableClient {

    /**
     * Fetches received receivable summaries based on received dates.
     * NOTE: This endpoint needs to be implemented in AccountsReceivableService.
     * It should return a List<ReceivableSummaryDTO> with UUID IDs.
     * Busca resumos de contas a receber recebidas com base nas datas de recebimento.
     * NOTA: Este endpoint precisa ser implementado no AccountsReceivableService.
     * Ele deve retornar List<ReceivableSummaryDTO> com IDs UUID.
     */
    // <<< PATH SUGEERIDO para o Controller do AR >>>
    @GetMapping("/api/receivables/summary-by-received-date") // Endpoint sugerido a ser criado no AR
    List<ReceivableSummaryDTO> getReceivablesSummaryByReceivedDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    /**
     * Fetches pending receivable summaries based on due dates.
     * NOTE: This endpoint needs to be implemented in AccountsReceivableService.
     * It should return a List<ReceivableSummaryDTO> with UUID IDs.
     * Busca resumos de contas a receber pendentes com base nas datas de vencimento.
     * NOTA: Este endpoint precisa ser implementado no AccountsReceivableService.
     * Ele deve retornar List<ReceivableSummaryDTO> com IDs UUID.
     */
    // <<< PATH SUGEERIDO para o Controller do AR >>>
    @GetMapping("/api/receivables/pending-summary-by-due-date") // Endpoint sugerido a ser criado no AR
    List<ReceivableSummaryDTO> getPendingReceivablesSummaryByDueDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}