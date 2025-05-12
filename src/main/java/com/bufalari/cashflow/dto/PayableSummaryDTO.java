package com.bufalari.cashflow.dto;

import com.bufalari.cashflow.enums.PayableStatus; // Enum copy within this service
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Simplified DTO representing payable data needed for cash flow calculation.
 * Received from AccountsPayableService (which should return UUID IDs).
 * DTO simplificado representando dados de contas a pagar necessários para cálculo de fluxo de caixa.
 * Recebido do AccountsPayableService (que deve retornar IDs UUID).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayableSummaryDTO {

    @Schema(description = "Unique identifier (UUID) of the original Payable", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id; // <<<--- Changed to UUID

    @Schema(description = "Original due date of the payable", example = "2024-05-15")
    private LocalDate dueDate;

    @Schema(description = "Original total amount due for the payable", example = "2500.00")
    private BigDecimal amountDue; // Original amount

    @Schema(description = "Amount actually paid in the relevant transaction (for statement) or total paid so far (for forecast)", example = "1000.00")
    private BigDecimal amountPaid; // Amount paid in the transaction OR total paid so far

    @Schema(description = "Current status of the original payable", example = "PARTIALLY_PAID")
    private PayableStatus status; // Uses local enum copy

    @Schema(description = "Date the specific payment occurred (relevant for statement)", example = "2024-05-10", nullable = true)
    private LocalDate paymentDate; // Date when THIS payment happened (for statement)
}