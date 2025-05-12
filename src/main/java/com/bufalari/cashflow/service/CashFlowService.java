package com.bufalari.cashflow.service;

import com.bufalari.cashflow.client.AccountsPayableClient;
import com.bufalari.cashflow.client.AccountsReceivableClient;
import com.bufalari.cashflow.converter.ManualCashEntryConverter;
import com.bufalari.cashflow.dto.*; // Import all DTOs
import com.bufalari.cashflow.entity.ManualCashEntry;
import com.bufalari.cashflow.enums.EntryType;
import com.bufalari.cashflow.enums.PayableStatus; // Import enums used in DTOs
import com.bufalari.cashflow.enums.ReceivableStatus; // Import enums used in DTOs
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
 * Integrates data from AP, AR, and Manual Entries. Uses UUID where applicable.
 * Serviço responsável por calcular e projetar o fluxo de caixa.
 * Integra dados de AP, AR e Lançamentos Manuais. Usa UUID onde aplicável.
 */
@Service
@RequiredArgsConstructor // Injeta final fields via constructor
@Transactional // Default transactional behavior
public class CashFlowService {

    private static final Logger log = LoggerFactory.getLogger(CashFlowService.class);

    // Repositories & Converters
    private final ManualCashEntryRepository manualCashEntryRepository;
    private final ManualCashEntryConverter manualCashEntryConverter;

    // Feign Clients
    private final AccountsPayableClient payableClient;
    private final AccountsReceivableClient receivableClient;

    // --- Manual Entry Management (using UUID) ---

    /**
     * Creates a new manual cash entry.
     * Cria um novo lançamento manual de caixa.
     * @param dto DTO containing entry data (ID should be null). / DTO contendo dados do lançamento (ID deve ser nulo).
     * @return The created DTO with its generated UUID. / O DTO criado com seu UUID gerado.
     */
    public ManualCashEntryDTO createManualEntry(ManualCashEntryDTO dto) {
        log.info("Creating manual cash entry: {}", dto.getDescription());
        if (dto.getId() != null) {
            log.warn("Attempted to create manual entry with existing ID {}. ID will be ignored.", dto.getId());
            dto.setId(null); // Ensure ID is null
        }
        ManualCashEntry entity = manualCashEntryConverter.dtoToEntity(dto);
        // @PrePersist/Update in entity validates amount
        ManualCashEntry saved = manualCashEntryRepository.save(entity);
        log.info("Manual cash entry created with ID: {}", saved.getId());
        return manualCashEntryConverter.entityToDTO(saved);
    }

    /**
     * Retrieves a manual cash entry by its UUID.
     * Recupera um lançamento manual de caixa por seu UUID.
     * @param id The UUID of the entry. / O UUID do lançamento.
     * @return The found DTO. / O DTO encontrado.
     * @throws ResourceNotFoundException If not found. / Se não encontrado.
     */
    @Transactional(readOnly = true)
    public ManualCashEntryDTO getManualEntryById(UUID id) { // <<<--- UUID
        log.debug("Fetching manual entry by ID: {}", id);
        return manualCashEntryRepository.findById(id) // <<<--- Use findById with UUID
                .map(manualCashEntryConverter::entityToDTO)
                .orElseThrow(() -> {
                    log.warn("Manual cash entry not found with ID: {}", id);
                    return new ResourceNotFoundException("Manual cash entry not found with ID: " + id);
                });
    }

    /**
     * Deletes a manual cash entry by its UUID.
     * Deleta um lançamento manual de caixa por seu UUID.
     * @param id The UUID of the entry to delete. / O UUID do lançamento a ser deletado.
     * @throws ResourceNotFoundException If not found. / Se não encontrado.
     */
    public void deleteManualEntry(UUID id) { // <<<--- UUID
        log.info("Attempting to delete manual entry with ID: {}", id);
        if (!manualCashEntryRepository.existsById(id)) { // <<<--- Use existsById with UUID
            log.warn("Delete failed: Manual cash entry not found with ID: {}", id);
            throw new ResourceNotFoundException("Manual cash entry not found with ID: " + id);
        }
        manualCashEntryRepository.deleteById(id); // <<<--- Use deleteById with UUID
        log.info("Manual entry deleted successfully with ID: {}", id);
    }


    // --- Cash Flow Calculation and Projection ---

    /**
     * Calculates the estimated current cash balance based on past transactions up to yesterday.
     * Calcula o saldo de caixa atual estimado baseado em transações passadas até ontem.
     * @param openingBalanceDate The date of the known opening balance. / A data do saldo inicial conhecido.
     * @param openingBalance The known balance on the openingBalanceDate. / O saldo conhecido na openingBalanceDate.
     * @return The calculated balance as of the end of yesterday. / O saldo calculado até o final de ontem.
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(LocalDate openingBalanceDate, BigDecimal openingBalance) {
        log.debug("Calculating current balance starting from {} with balance {}", openingBalanceDate, openingBalance);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Ensure opening balance date is not in the future relative to 'yesterday'
        if (openingBalanceDate.isAfter(yesterday)) {
            log.warn("Opening balance date {} is after yesterday {}. Current balance calculation might be inaccurate or nonsensical.", openingBalanceDate, yesterday);
            // Decide handling: return openingBalance? Throw error? Calculate up to openingBalanceDate?
            // For now, return opening balance as the "current" balance in this edge case.
            return openingBalance;
        }

        // Use the statement logic to get net flow between opening date and yesterday (inclusive)
        CashFlowStatementDTO statement = getCashFlowStatement(openingBalanceDate, yesterday, openingBalance);

        BigDecimal currentBalance = statement.getClosingBalance();
        log.info("Calculated current balance as of {}: {}", yesterday, currentBalance);
        return currentBalance;
    }

    /**
     * Generates a cash flow statement for a given period (inclusive).
     * Gera um demonstrativo de fluxo de caixa para um período específico (inclusivo).
     * @param startDate Start date of the period. / Data de início do período.
     * @param endDate End date of the period. / Data de fim do período.
     * @param openingBalance Cash balance at the beginning of the startDate. / Saldo de caixa no início de startDate.
     * @return A DTO representing the cash flow statement. / Um DTO representando o demonstrativo.
     */
    @Transactional(readOnly = true)
    public CashFlowStatementDTO getCashFlowStatement(LocalDate startDate, LocalDate endDate, BigDecimal openingBalance) {
        log.info("Generating cash flow statement from {} to {} with opening balance {}", startDate, endDate, openingBalance);

        // 1. Fetch Paid Payables based on Payment Date within the range
        List<PayableSummaryDTO> paidPayables = new ArrayList<>();
        try {
            paidPayables = payableClient.getPayablesSummaryByPaymentDateRange(startDate, endDate);
            log.debug("Fetched {} paid payable summaries from AP service.", paidPayables.size());
        } catch (Exception e) {
            log.error("Failed to fetch paid payables from AccountsPayableService between {} and {}: {}", startDate, endDate, e.getMessage(), e);
            // Decide handling: continue with empty list, throw exception?
            // Continuing with empty list for now.
        }

        // 2. Fetch Received Receivables based on Received Date within the range
        List<ReceivableSummaryDTO> receivedReceivables = new ArrayList<>();
        try {
            // NOTE: Assumes AR service has '/api/receivables/summary-by-received-date' endpoint
            receivedReceivables = receivableClient.getReceivablesSummaryByReceivedDateRange(startDate, endDate);
            log.debug("Fetched {} received receivable summaries from AR service.", receivedReceivables.size());
        } catch (Exception e) {
            log.error("Failed to fetch received receivables from AccountsReceivableService between {} and {}: {}", startDate, endDate, e.getMessage(), e);
            // Continuing with empty list for now.
        }


        // 3. Fetch Manual Entries within the date range
        List<ManualCashEntry> manualEntries = manualCashEntryRepository.findByEntryDateBetweenOrderByEntryDateAsc(startDate, endDate);
        log.debug("Fetched {} manual cash entries between {} and {}", manualEntries.size(), startDate, endDate);

        // 4. Build CashFlowItemDTO lists for inflows and outflows
        List<CashFlowItemDTO> inflowItems = new ArrayList<>();
        List<CashFlowItemDTO> outflowItems = new ArrayList<>();

        // Process Inflows (Received Receivables + Manual Credits)
        receivedReceivables.forEach(r -> inflowItems.add(new CashFlowItemDTO(
                r.getReceivedDate(), // Date it was received
                "Receivable: " + r.getId(), // Description uses UUID
                r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO, // Use amount received if available
                "RECEIVABLE",
                r.getId() // <<<--- Use UUID
        )));
        manualEntries.stream()
                .filter(m -> m.getType() == EntryType.CREDIT)
                .forEach(m -> inflowItems.add(new CashFlowItemDTO(
                        m.getEntryDate(),
                        m.getDescription(),
                        m.getAmount(),
                        "MANUAL_CREDIT",
                        m.getId() // <<<--- Use UUID
                )));

        // Process Outflows (Paid Payables + Manual Debits)
        paidPayables.forEach(p -> outflowItems.add(new CashFlowItemDTO(
                p.getPaymentDate(), // Date it was paid
                "Payable: " + p.getId(), // Description uses UUID
                p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO, // Use amount paid
                "PAYABLE",
                p.getId() // <<<--- Use UUID
        )));
        manualEntries.stream()
                .filter(m -> m.getType() == EntryType.DEBIT)
                .forEach(m -> outflowItems.add(new CashFlowItemDTO(
                        m.getEntryDate(),
                        m.getDescription(),
                        m.getAmount(),
                        "MANUAL_DEBIT",
                        m.getId() // <<<--- Use UUID
                )));

        // 5. Calculate totals
        BigDecimal totalInflows = inflowItems.stream().map(CashFlowItemDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutflows = outflowItems.stream().map(CashFlowItemDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netCashFlow = totalInflows.subtract(totalOutflows);
        BigDecimal closingBalance = openingBalance.add(netCashFlow);

        log.info("Cash Flow Statement ({}-{}): Opening={}, Inflows={}, Outflows={}, Net={}, Closing={}",
                startDate, endDate, openingBalance, totalInflows, totalOutflows, netCashFlow, closingBalance);

        // Sort items by date for the report
        inflowItems.sort(Comparator.comparing(CashFlowItemDTO::getDate));
        outflowItems.sort(Comparator.comparing(CashFlowItemDTO::getDate));

        return CashFlowStatementDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .openingBalance(openingBalance)
                .totalInflows(totalInflows)
                .totalOutflows(totalOutflows)
                .netCashFlow(netCashFlow)
                .closingBalance(closingBalance)
                .inflowItems(inflowItems)
                .outflowItems(outflowItems)
                .build();
    }

    /**
     * Projects the cash flow for a specified number of days into the future from today.
     * Projeta o fluxo de caixa para um número especificado de dias no futuro a partir de hoje.
     * @param daysAhead Number of days to forecast. / Número de dias para projetar.
     * @param currentBalance Starting balance for the forecast (usually today's opening balance). / Saldo inicial para a projeção.
     * @return A DTO containing the daily projected balances. / Um DTO contendo os saldos diários projetados.
     */
    @Transactional(readOnly = true)
    public CashFlowForecastDTO getCashFlowForecast(int daysAhead, BigDecimal currentBalance) {
        log.info("Generating cash flow forecast for {} days ahead, starting balance {}", daysAhead, currentBalance);
        LocalDate today = LocalDate.now();
        LocalDate forecastEndDate = today.plusDays(daysAhead); // End date is exclusive for stream limit

        // 1. Get Pending Payables due within the forecast period
        List<PayableSummaryDTO> pendingPayables = new ArrayList<>();
        try {
            pendingPayables = payableClient.getPendingPayablesSummaryByDueDateRange(today, forecastEndDate);
            log.debug("Fetched {} pending payable summaries for forecast.", pendingPayables.size());
        } catch (Exception e) {
            log.error("Failed to fetch pending payables from AP service for forecast: {}", e.getMessage(), e);
            // Continue with empty list
        }

        // 2. Get Pending Receivables due within the forecast period
        List<ReceivableSummaryDTO> pendingReceivables = new ArrayList<>();
        try {
            // NOTE: Assumes AR service has '/api/receivables/pending-summary-by-due-date' endpoint
            pendingReceivables = receivableClient.getPendingReceivablesSummaryByDueDateRange(today, forecastEndDate);
            log.debug("Fetched {} pending receivable summaries for forecast.", pendingReceivables.size());
        } catch (Exception e) {
            log.error("Failed to fetch pending receivables from AR service for forecast: {}", e.getMessage(), e);
            // Continue with empty list
        }

        // 3. Get Future-dated Manual Entries (Optional - if needed)
        // List<ManualCashEntry> futureManualEntries = manualCashEntryRepository.findByEntryDateBetweenOrderByEntryDateAsc(today, forecastEndDate);
        // log.debug("Fetched {} future manual entries for forecast.", futureManualEntries.size());

        // 4. Calculate expected net flow for each day in the forecast period
        Map<LocalDate, BigDecimal> dailyNetFlow = new HashMap<>();

        // Process expected inflows (Pending Receivables)
        pendingReceivables.forEach(r -> {
            // Calculate remaining amount expected
            BigDecimal remaining = r.getAmountExpected().subtract(r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO);
            // Only consider positive remaining amounts expected by the due date
            if (remaining.compareTo(BigDecimal.ZERO) > 0 && r.getDueDate() != null) {
                dailyNetFlow.merge(r.getDueDate(), remaining, BigDecimal::add); // Add expected inflow on due date
            }
        });

        // Process expected outflows (Pending Payables)
        pendingPayables.forEach(p -> {
            // Calculate remaining amount due
            BigDecimal remaining = p.getAmountDue().subtract(p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO);
            // Only consider positive remaining amounts due by the due date
            if (remaining.compareTo(BigDecimal.ZERO) > 0 && p.getDueDate() != null) {
                dailyNetFlow.merge(p.getDueDate(), remaining.negate(), BigDecimal::add); // Subtract expected outflow on due date
            }
        });

        // Process future manual entries (if fetched)
        // futureManualEntries.forEach(m -> {
        //     BigDecimal amount = (m.getType() == EntryType.CREDIT) ? m.getAmount() : m.getAmount().negate();
        //     dailyNetFlow.merge(m.getEntryDate(), amount, BigDecimal::add);
        // });

        // 5. Calculate cumulative projected balance day by day
        Map<LocalDate, BigDecimal> dailyProjectedBalance = new TreeMap<>(); // Use TreeMap to keep dates sorted
        BigDecimal runningBalance = currentBalance;
        for (LocalDate date = today; !date.isAfter(forecastEndDate); date = date.plusDays(1)) {
            runningBalance = runningBalance.add(dailyNetFlow.getOrDefault(date, BigDecimal.ZERO));
            dailyProjectedBalance.put(date, runningBalance);
            log.trace("Forecast - Date: {}, Daily Net: {}, Projected Balance: {}", date, dailyNetFlow.getOrDefault(date, BigDecimal.ZERO), runningBalance);
        }

        log.info("Cash flow forecast generated up to {}", forecastEndDate);

        return new CashFlowForecastDTO(today, currentBalance, dailyProjectedBalance);
    }

}