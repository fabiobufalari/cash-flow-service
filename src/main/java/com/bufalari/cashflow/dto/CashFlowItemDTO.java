package com.bufalari.cashflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Represents a single item contributing to the cash flow (inflow or outflow).
 * Representa um item individual que contribui para o fluxo de caixa (entrada ou saída).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowItemDTO {

    @Schema(description = "Date the cash flow occurred or is expected", example = "2024-05-10")
    private LocalDate date; // Date the flow occurred or is expected

    @Schema(description = "Description of the item", example = "Payment Received - Invoice INV-001")
    private String description;

    @Schema(description = "Amount of the cash flow (always positive)", example = "1500.00")
    private BigDecimal amount; // Always positive, type indicates direction

    @Schema(description = "Type of the cash flow source", example = "RECEIVABLE", allowableValues = {"RECEIVABLE", "PAYABLE", "MANUAL_CREDIT", "MANUAL_DEBIT"})
    private String type; // e.g., "RECEIVABLE", "PAYABLE", "MANUAL_CREDIT", "MANUAL_DEBIT"

    @Schema(description = "UUID of the related Receivable, Payable, or Manual Entry", example = "123e4567-e89b-12d3-a456-426614174000", nullable = true)
    private UUID relatedId; // <<<--- Changed to UUID
}