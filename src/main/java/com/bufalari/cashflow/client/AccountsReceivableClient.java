// Path: src/main/java/com/bufalari/cashflow/client/AccountsReceivableClient.java
package com.bufalari.cashflow.client;

import com.bufalari.cashflow.dto.ReceivableSummaryDTO; // Import summary DTO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Feign client for interacting with the Accounts Receivable Service.
 * Cliente Feign para interagir com o Serviço de Contas a Receber.
 */
@FeignClient(name = "accounts-receivable-client-cf", url = "${receivable.service.url}") // Needs receivable.service.url in application.yml
public interface AccountsReceivableClient {

    /**
     * Retrieves a list of receivable summaries within a date range (received or due).
     * Recupera uma lista de resumos de contas a receber dentro de um intervalo de datas (recebidas ou a vencer).
     * NOTE: This endpoint needs to be implemented in accounts-receivable-service.
     * NOTA: Este endpoint precisa ser implementado no accounts-receivable-service.
     */
    @GetMapping("/api/receivables/summary-by-date") // Example endpoint
    List<ReceivableSummaryDTO> getReceivablesSummaryByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    /**
     * Retrieves a list of pending receivable summaries due within a date range.
     * Recupera uma lista de resumos de contas a receber pendentes com vencimento em um intervalo de datas.
     * NOTE: This endpoint needs to be implemented in accounts-receivable-service.
     * NOTA: Este endpoint precisa ser implementado no accounts-receivable-service.
     */
     @GetMapping("/api/receivables/pending-summary-by-due-date") // Example endpoint
     List<ReceivableSummaryDTO> getPendingReceivablesSummaryByDueDateRange(
             @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
             @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}