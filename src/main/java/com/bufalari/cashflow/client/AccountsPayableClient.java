package com.bufalari.cashflow.client;

import com.bufalari.cashflow.dto.PayableSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "accounts-payable-client-cf", url = "${payable.service.url}")
public interface AccountsPayableClient {

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/payables/summary-by-date")
    List<PayableSummaryDTO> getPayablesSummaryByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/payables/pending-summary-by-due-date")
    List<PayableSummaryDTO> getPendingPayablesSummaryByDueDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}