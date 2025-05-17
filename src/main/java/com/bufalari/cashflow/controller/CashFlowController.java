package com.bufalari.cashflow.controller;

import com.bufalari.cashflow.dto.CashFlowBalanceDTO; // Não usado diretamente, mas implícito
import com.bufalari.cashflow.dto.CashFlowForecastDTO;
import com.bufalari.cashflow.dto.CashFlowStatementDTO;
import com.bufalari.cashflow.dto.ManualCashEntryDTO;
import com.bufalari.cashflow.service.CashFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * REST controller for managing cash flow, including manual entries and reports.
 * Controlador REST para gerenciamento de fluxo de caixa, incluindo lançamentos manuais e relatórios.
 */
@RestController
@RequestMapping("/cash-flow")
@RequiredArgsConstructor
@Tag(name = "Cash Flow Management", description = "Endpoints for manual cash entries and cash flow reporting / Endpoints para lançamentos manuais e relatórios de fluxo de caixa")
@SecurityRequirement(name = "bearerAuth") // Assume global security via JWT
public class CashFlowController {

    private static final Logger log = LoggerFactory.getLogger(CashFlowController.class);
    private final CashFlowService cashFlowService;

    // --- Manual Cash Entries ---

    @Operation(summary = "Create Manual Cash Entry", description = "Creates a manual cash debit or credit entry. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Manual entry created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ManualCashEntryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping(value = "/manual-entries", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ManualCashEntryDTO> createManualEntry(@Valid @RequestBody ManualCashEntryDTO manualEntryDTO) {
        log.info("Request received to create manual cash entry: {}", manualEntryDTO.getDescription());
        ManualCashEntryDTO createdEntry = cashFlowService.createManualEntry(manualEntryDTO);
        // Build URI pointing to the newly created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // Starts from /api/cashflow/manual-entries
                .path("/{id}")
                .buildAndExpand(createdEntry.getId()) // Use the UUID from the created DTO
                .toUri();
        log.info("Manual cash entry created with ID {} at location {}", createdEntry.getId(), location);
        return ResponseEntity.created(location).body(createdEntry);
    }

    @Operation(summary = "Get Manual Cash Entry by ID", description = "Retrieves a specific manual cash entry by its UUID. Requires ACCOUNTANT, ADMIN, or FINANCIAL_VIEWER role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manual entry found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ManualCashEntryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Manual entry not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/manual-entries/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<ManualCashEntryDTO> getManualEntryById(
            @Parameter(description = "UUID of the manual cash entry") @PathVariable UUID id) { // <<<--- UUID
        log.debug("Request received to get manual entry ID: {}", id);
        return ResponseEntity.ok(cashFlowService.getManualEntryById(id));
    }

    @Operation(summary = "Delete Manual Cash Entry", description = "Deletes a specific manual cash entry by its UUID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Manual entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Manual entry not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/manual-entries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteManualEntry(
            @Parameter(description = "UUID of the manual cash entry to delete") @PathVariable UUID id) { // <<<--- UUID
        log.info("Request received to delete manual entry ID: {}", id);
        cashFlowService.deleteManualEntry(id);
        return ResponseEntity.noContent().build();
    }

    // --- Cash Flow Reporting ---

    @Operation(summary = "Get Current Cash Balance", description = "Calculates the estimated current cash balance based on past transactions up to yesterday, starting from a known opening balance. Requires authenticated access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current balance calculated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "400", description = "Invalid opening balance parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Error during calculation (e.g., external service communication)")
    })
    @GetMapping(value = "/balance/current", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Adjust roles as needed
    public ResponseEntity<BigDecimal> getCurrentBalance(
            @Parameter(description = "Date for the known opening balance (YYYY-MM-DD)", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openingBalanceDate,
            @Parameter(description = "Known cash balance on the opening balance date", example = "10000.00", required = true)
            @RequestParam BigDecimal openingBalance) {
        log.debug("Request received for current cash balance, starting from {} with balance {}", openingBalanceDate, openingBalance);
        BigDecimal balance = cashFlowService.getCurrentBalance(openingBalanceDate, openingBalance);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Get Cash Flow Statement", description = "Generates a cash flow statement for a specified period, starting from a known opening balance. Requires authenticated access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statement generated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CashFlowStatementDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range or opening balance parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Error during statement generation (e.g., external service communication)")
    })
    @GetMapping(value = "/statement", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Adjust roles as needed
    public ResponseEntity<CashFlowStatementDTO> getCashFlowStatement(
            @Parameter(description = "Start date for the statement (YYYY-MM-DD, inclusive)", example="2024-04-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for the statement (YYYY-MM-DD, inclusive)", example="2024-04-30", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Known cash balance at the beginning of the start date", example="15000.50", required = true)
            @RequestParam BigDecimal openingBalance) {
        log.debug("Request received for cash flow statement from {} to {}", startDate, endDate);
        if (startDate.isAfter(endDate)) {
            log.warn("Invalid date range requested for cash flow statement: startDate ({}) is after endDate ({})", startDate, endDate);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before or equal to end date");
        }
        CashFlowStatementDTO statement = cashFlowService.getCashFlowStatement(startDate, endDate, openingBalance);
        return ResponseEntity.ok(statement);
    }

    @Operation(summary = "Get Cash Flow Forecast", description = "Projects cash flow for a number of days ahead based on pending receivables/payables and a current balance. Requires authenticated access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forecast generated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CashFlowForecastDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid days ahead or current balance"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Error during forecast generation (e.g., external service communication)")
    })
    @GetMapping(value = "/forecast", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Adjust roles as needed
    public ResponseEntity<CashFlowForecastDTO> getCashFlowForecast(
            @Parameter(description = "Number of days to forecast ahead from today", example = "30", required = true)
            @RequestParam(defaultValue = "30") int daysAhead,
            @Parameter(description = "Current known cash balance to start the forecast from", example="25750.75", required = true)
            @RequestParam BigDecimal currentBalance) {
        log.debug("Request received for cash flow forecast for {} days", daysAhead);
        if (daysAhead <= 0) {
            log.warn("Invalid forecast request: daysAhead ({}) must be positive", daysAhead);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Days ahead must be positive");
        }
        CashFlowForecastDTO forecast = cashFlowService.getCashFlowForecast(daysAhead, currentBalance);
        return ResponseEntity.ok(forecast);
    }
}