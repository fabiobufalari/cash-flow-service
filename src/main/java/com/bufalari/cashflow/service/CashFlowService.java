package com.bufalari.cashflow.service;


import com.bufalari.cashflow.client.AccountsPayableClient;
import com.bufalari.cashflow.client.AccountsReceivableClient;
import com.bufalari.cashflow.converter.ManualCashEntryConverter;
import com.bufalari.cashflow.dto.*; // Import all DTOs
import com.bufalari.cashflow.entity.ManualCashEntry;
import com.bufalari.cashflow.enums.EntryType;

import com.bufalari.cashflow.exception.ResourceNotFoundException;
import com.bufalari.cashflow.repository.ManualCashEntryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service responsible for calculating and projecting cash flow.
 * Serviço responsável por calcular e projetar o fluxo de caixa.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CashFlowService {

    private static final Logger log = LoggerFactory.getLogger(CashFlowService.class);

    private final ManualCashEntryRepository manualCashEntryRepository;
    private final ManualCashEntryConverter manualCashEntryConverter;
    private final AccountsPayableClient payableClient; // Inject Feign clients
    private final AccountsReceivableClient receivableClient;

    // --- Manual Entry Management ---

    public ManualCashEntryDTO createManualEntry(ManualCashEntryDTO dto) {
        log.info("Creating manual cash entry: {}", dto.getDescription());
        ManualCashEntry entity = manualCashEntryConverter.dtoToEntity(dto);
        ManualCashEntry saved = manualCashEntryRepository.save(entity);
        log.info("Manual cash entry created with ID: {}", saved.getId());
        return manualCashEntryConverter.entityToDTO(saved);
    }

    @Transactional(readOnly = true)
    public ManualCashEntryDTO getManualEntryById(Long id) {
        log.debug("Fetching manual entry by ID: {}", id);
        return manualCashEntryRepository.findById(id)
                .map(manualCashEntryConverter::entityToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Manual cash entry not found with ID: " + id));
    }

    public void deleteManualEntry(Long id) {
        log.info("Deleting manual entry with ID: {}", id);
         if (!manualCashEntryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Manual cash entry not found with ID: " + id);
        }
        manualCashEntryRepository.deleteById(id);
        log.info("Manual entry deleted successfully with ID: {}", id);
    }


    // --- Cash Flow Calculation and Projection ---

    /**
     * Calculates the current cash balance based on past transactions.
     * Calcula o saldo de caixa atual baseado em transações passadas.
     * Note: This requires a starting balance or calculating from the beginning of time.
     * Nota: Requer um saldo inicial ou calcular desde o início dos tempos.
     * This implementation calculates the net flow up to yesterday.
     * Esta implementação calcula o fluxo líquido até ontem.
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(LocalDate openingBalanceDate, BigDecimal openingBalance) {
        log.debug("Calculating current balance starting from {} with balance {}", openingBalanceDate, openingBalance);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Use the statement logic to get net flow up to yesterday
        CashFlowStatementDTO statement = getCashFlowStatement(openingBalanceDate, yesterday, openingBalance);

        BigDecimal currentBalance = statement.getClosingBalance();
        log.info("Calculated current balance as of {}: {}", yesterday, currentBalance);
        return currentBalance;
    }

    /**
     * Generates a cash flow statement for a given period.
     * Gera um demonstrativo de fluxo de caixa para um período específico.
     */
    @Transactional(readOnly = true)
    public CashFlowStatementDTO getCashFlowStatement(LocalDate startDate, LocalDate endDate, BigDecimal openingBalance) {
        log.info("Generating cash flow statement from {} to {} with opening balance {}", startDate, endDate, openingBalance);

        // 1. Fetch paid Payables within the date range (using paymentDate)
        List<PayableSummaryDTO> paidPayables = new ArrayList<>(); // payableClient.getPayablesSummaryByDateRange(startDate, endDate); // Needs refinement in AP service
        List<PayableSummaryDTO> actualPaidPayables = paidPayables.stream()
             .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().isBefore(startDate) && !p.getPaymentDate().isAfter(endDate))
             .toList(); // Filter for actual payment date within range

        // 2. Fetch received Receivables within the date range (using receivedDate)
        List<ReceivableSummaryDTO> receivedReceivables = new ArrayList<>(); // receivableClient.getReceivablesSummaryByDateRange(startDate, endDate); // Needs refinement in AR service
        List<ReceivableSummaryDTO> actualReceivedReceivables = receivedReceivables.stream()
             .filter(r -> r.getReceivedDate() != null && !r.getReceivedDate().isBefore(startDate) && !r.getReceivedDate().isAfter(endDate))
             .toList(); // Filter for actual received date within range

        // 3. Fetch Manual Entries within the date range
        List<ManualCashEntry> manualEntries = manualCashEntryRepository.findByEntryDateBetweenOrderByEntryDateAsc(startDate, endDate);

        // 4. Build CashFlowItemDTO lists for inflows and outflows
        List<CashFlowItemDTO> inflowItems = new ArrayList<>();
        List<CashFlowItemDTO> outflowItems = new ArrayList<>();

        actualReceivedReceivables.forEach(r -> inflowItems.add(new CashFlowItemDTO(
                r.getReceivedDate(),
                "Receivable ID: " + r.getId(), // Simple description
                r.getAmountReceived(), // Amount actually received
                "RECEIVABLE",
                r.getId()
        )));
        manualEntries.stream().filter(m -> m.getType() == EntryType.CREDIT).forEach(m -> inflowItems.add(new CashFlowItemDTO(
                m.getEntryDate(),
                m.getDescription(),
                m.getAmount(),
                "MANUAL_CREDIT",
                m.getId()
        )));

        actualPaidPayables.forEach(p -> outflowItems.add(new CashFlowItemDTO(
                p.getPaymentDate(),
                 "Payable ID: " + p.getId(), // Simple description
                p.getAmountPaid(), // Amount actually paid
                "PAYABLE",
                p.getId()
        )));
         manualEntries.stream().filter(m -> m.getType() == EntryType.DEBIT).forEach(m -> outflowItems.add(new CashFlowItemDTO(
                m.getEntryDate(),
                m.getDescription(),
                m.getAmount(),
                "MANUAL_DEBIT",
                m.getId()
        )));

        // 5. Calculate totals
        BigDecimal totalInflows = inflowItems.stream().map(CashFlowItemDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutflows = outflowItems.stream().map(CashFlowItemDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netCashFlow = totalInflows.subtract(totalOutflows);
        BigDecimal closingBalance = openingBalance.add(netCashFlow);

        log.info("Statement calculated: Inflows={}, Outflows={}, Net={}, Closing={}", totalInflows, totalOutflows, netCashFlow, closingBalance);

        return CashFlowStatementDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .openingBalance(openingBalance)
                .totalInflows(totalInflows)
                .totalOutflows(totalOutflows)
                .netCashFlow(netCashFlow)
                .closingBalance(closingBalance)
                .inflowItems(inflowItems.stream().sorted(Comparator.comparing(CashFlowItemDTO::getDate)).collect(Collectors.toList()))
                .outflowItems(outflowItems.stream().sorted(Comparator.comparing(CashFlowItemDTO::getDate)).collect(Collectors.toList()))
                .build();
    }

    /**
     * Projects the cash flow for a specified number of days into the future.
     * Projeta o fluxo de caixa para um número especificado de dias no futuro.
     */
    @Transactional(readOnly = true)
    public CashFlowForecastDTO getCashFlowForecast(int daysAhead, BigDecimal currentBalance) {
         log.info("Generating cash flow forecast for {} days ahead, starting balance {}", daysAhead, currentBalance);
         LocalDate today = LocalDate.now();
         LocalDate forecastEndDate = today.plusDays(daysAhead);

        // 1. Get pending payables due within the forecast period
        List<PayableSummaryDTO> pendingPayables = new ArrayList<>(); // payableClient.getPendingPayablesSummaryByDueDateRange(today, forecastEndDate); // Needs implementation in AP service

        // 2. Get pending receivables due within the forecast period
        List<ReceivableSummaryDTO> pendingReceivables = new ArrayList<>(); // receivableClient.getPendingReceivablesSummaryByDueDateRange(today, forecastEndDate); // Needs implementation in AR service

        // 3. Get future-dated manual entries (if any)
        // TODO: Add logic to fetch future manual entries if applicable

        // 4. Calculate daily net flow and projected balance
        Map<LocalDate, BigDecimal> dailyNetFlow = Stream.iterate(today, date -> date.plusDays(1))
                .limit(daysAhead + 1) // Include today up to forecastEndDate
                .collect(Collectors.toMap(date -> date, date -> BigDecimal.ZERO));

        pendingReceivables.forEach(r -> {
            BigDecimal remaining = r.getAmountExpected().subtract(r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                 dailyNetFlow.compute(r.getDueDate(), (date, currentFlow) -> (currentFlow == null ? BigDecimal.ZERO : currentFlow).add(remaining));
            }
        });

        pendingPayables.forEach(p -> {
             BigDecimal remaining = p.getAmountDue().subtract(p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO);
             if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                 dailyNetFlow.compute(p.getDueDate(), (date, currentFlow) -> (currentFlow == null ? BigDecimal.ZERO : currentFlow).subtract(remaining));
             }
        });

        // TODO: Add future manual entries to dailyNetFlow

        // 5. Calculate cumulative projected balance
        Map<LocalDate, BigDecimal> dailyProjectedBalance = new TreeMap<>(); // Use TreeMap to keep dates sorted
        BigDecimal runningBalance = currentBalance;
        for (LocalDate date = today; !date.isAfter(forecastEndDate); date = date.plusDays(1)) {
             runningBalance = runningBalance.add(dailyNetFlow.getOrDefault(date, BigDecimal.ZERO));
             dailyProjectedBalance.put(date, runningBalance);
        }

        log.info("Cash flow forecast generated up to {}", forecastEndDate);

        return new CashFlowForecastDTO(today, currentBalance, dailyProjectedBalance);
    }

}