package com.bufalari.cashflow.client;

import com.bufalari.cashflow.dto.ReceivableSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "accounts-receivable-client-cf", url = "${receivable.service.url}")
public interface AccountsReceivableClient {

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/receivables/summary-by-date") // Endpoint a ser criado no AR
    List<ReceivableSummaryDTO> getReceivablesSummaryByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/receivables/pending-summary-by-due-date") // Endpoint a ser criado no AR
    List<ReceivableSummaryDTO> getPendingReceivablesSummaryByDueDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}