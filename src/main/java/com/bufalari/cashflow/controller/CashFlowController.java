// Path: src/main/java/com/bufalari/cashflow/controller/CashFlowController.java
package com.bufalari.cashflow.controller;

import com.bufalari.cashflow.dto.CashFlowBalanceDTO;
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

/**
 * REST controller for managing cash flow, including manual entries and reports.
 * Controlador REST para gerenciamento de fluxo de caixa, incluindo lançamentos manuais e relatórios.
 */
@RestController
@RequestMapping("/api/cashflow")
@RequiredArgsConstructor
@Tag(name = "Cash Flow Management", description = "Endpoints for manual cash entries and cash flow reporting / Endpoints para lançamentos manuais e relatórios de fluxo de caixa")
@SecurityRequirement(name = "bearerAuth")
public class CashFlowController {

    private static final Logger log = LoggerFactory.getLogger(CashFlowController.class);
    private final CashFlowService cashFlowService;

    // --- Manual Cash Entries ---

    @Operation(summary = "Create Manual Cash Entry", description = "Creates a manual cash debit or credit entry. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Manual entry created", content = @Content(schema = @Schema(implementation = ManualCashEntryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping(value = "/manual-entries", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ManualCashEntryDTO> createManualEntry(@Valid @RequestBody ManualCashEntryDTO manualEntryDTO) {
        log.info("Request received to create manual cash entry: {}", manualEntryDTO.getDescription());
        ManualCashEntryDTO createdEntry = cashFlowService.createManualEntry(manualEntryDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdEntry.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdEntry);
    }

    @Operation(summary = "Get Manual Cash Entry by ID", description = "Retrieves a specific manual cash entry. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manual entry found", content = @Content(schema = @Schema(implementation = ManualCashEntryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Manual entry not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/manual-entries/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<ManualCashEntryDTO> getManualEntryById(@PathVariable Long id) {
        log.debug("Request received to get manual entry ID: {}", id);
        return ResponseEntity.ok(cashFlowService.getManualEntryById(id));
    }

    @Operation(summary = "Delete Manual Cash Entry", description = "Deletes a specific manual cash entry. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Manual entry deleted"),
            @ApiResponse(responseCode = "404", description = "Manual entry not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/manual-entries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteManualEntry(@PathVariable Long id) {
        log.info("Request received to delete manual entry ID: {}", id);
        cashFlowService.deleteManualEntry(id);
        return ResponseEntity.noContent().build();
    }

    // --- Cash Flow Reporting ---

    @Operation(summary = "Get Current Cash Balance", description = "Calculates the estimated current cash balance based on past transactions. Requires authenticated access.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current balance calculated", content = @Content(schema = @Schema(implementation = BigDecimal.class))), // Adjust schema if needed
        @ApiResponse(responseCode = "400", description = "Invalid opening balance parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Error during calculation")
    })
    @GetMapping(value = "/balance/current", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getCurrentBalance(
            @Parameter(description = "Date for the opening balance (YYYY-MM-DD)", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openingBalanceDate,
            @Parameter(description = "Known balance on the opening balance date", example = "10000.00", required = true)
            @RequestParam BigDecimal openingBalance) {
        log.debug("Request received for current cash balance");
        BigDecimal balance = cashFlowService.getCurrentBalance(openingBalanceDate, openingBalance);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Get Cash Flow Statement", description = "Generates a cash flow statement for a specified period. Requires authenticated access.")
     @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statement generated", content = @Content(schema = @Schema(implementation = CashFlowStatementDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date range or opening balance parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Error during statement generation")
    })
    @GetMapping(value = "/statement", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CashFlowStatementDTO> getCashFlowStatement(
            @Parameter(description = "Start date for the statement (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for the statement (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Known balance at the start date", required = true)
            @RequestParam BigDecimal openingBalance) {
        log.debug("Request received for cash flow statement from {} to {}", startDate, endDate);
         if (startDate.isAfter(endDate)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before or equal to end date");
         }
        CashFlowStatementDTO statement = cashFlowService.getCashFlowStatement(startDate, endDate, openingBalance);
        return ResponseEntity.ok(statement);
    }

    @Operation(summary = "Get Cash Flow Forecast", description = "Projects cash flow for a number of days ahead. Requires authenticated access.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Forecast generated", content = @Content(schema = @Schema(implementation = CashFlowForecastDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid days or current balance"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Error during forecast generation")
    })
    @GetMapping(value = "/forecast", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CashFlowForecastDTO> getCashFlowForecast(
            @Parameter(description = "Number of days to forecast ahead", example = "30", required = true)
            @RequestParam(defaultValue = "30") int daysAhead,
            @Parameter(description = "Current known cash balance", required = true)
            @RequestParam BigDecimal currentBalance) {
        log.debug("Request received for cash flow forecast for {} days", daysAhead);
         if (daysAhead <= 0) {
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Days ahead must be positive");
         }
        CashFlowForecastDTO forecast = cashFlowService.getCashFlowForecast(daysAhead, currentBalance);
        return ResponseEntity.ok(forecast);
    }
}