// Path: src/main/java/com/bufalari/cashflow/client/AccountsPayableClient.java
package com.bufalari.cashflow.client;

import com.bufalari.cashflow.dto.PayableSummaryDTO; // Import summary DTO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Feign client for interacting with the Accounts Payable Service.
 * Cliente Feign para interagir com o Serviço de Contas a Pagar.
 */
@FeignClient(name = "accounts-payable-client-cf", url = "${payable.service.url}") // Needs payable.service.url in application.yml
public interface AccountsPayableClient {

    /**
     * Retrieves a list of payable summaries within a date range (paid or due).
     * Recupera uma lista de resumos de contas a pagar dentro de um intervalo de datas (pagas ou a vencer).
     * NOTE: This endpoint needs to be implemented in accounts-payable-service.
     * NOTA: Este endpoint precisa ser implementado no accounts-payable-service.
     * @param startDate Start date for paidDate or dueDate filter. / Data inicial para filtro de paymentDate ou dueDate.
     * @param endDate End date for paidDate or dueDate filter. / Data final para filtro de paymentDate ou dueDate.
     * @return List of payable summaries. / Lista de resumos de contas a pagar.
     */
    @GetMapping("/api/payables/summary-by-date") // Example endpoint
    List<PayableSummaryDTO> getPayablesSummaryByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    /**
     * Retrieves a list of pending payable summaries due within a date range.
     * Recupera uma lista de resumos de contas a pagar pendentes com vencimento em um intervalo de datas.
     * NOTE: This endpoint needs to be implemented in accounts-payable-service.
     * NOTA: Este endpoint precisa ser implementado no accounts-payable-service.
     * @param startDate Start of the due date range. / Início do intervalo de vencimento.
     * @param endDate End of the due date range. / Fim do intervalo de vencimento.
     * @return List of pending payable summaries. / Lista de resumos de contas a pagar pendentes.
     */
     @GetMapping("/api/payables/pending-summary-by-due-date") // Example endpoint
     List<PayableSummaryDTO> getPendingPayablesSummaryByDueDateRange(
             @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
             @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

}