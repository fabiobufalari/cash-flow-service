package com.bufalari.cashflow.dto;

import com.bufalari.cashflow.enums.ReceivableStatus; // Enum copy within this service
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Simplified DTO representing receivable data needed for cash flow calculation.
 * Received from AccountsReceivableService (which should return UUID IDs).
 * DTO simplificado representando dados de contas a receber necessários para cálculo de fluxo de caixa.
 * Recebido do AccountsReceivableService (que deve retornar IDs UUID).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableSummaryDTO {

    @Schema(description = "Unique identifier (UUID) of the original Receivable", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id; // <<<--- Changed to UUID

    @Schema(description = "Original due date of the receivable", example = "2024-06-10")
    private LocalDate dueDate;

    @Schema(description = "Original total amount expected for the receivable", example = "5000.00")
    private BigDecimal amountExpected;

    @Schema(description = "Amount actually received so far", example = "3000.00", nullable = true)
    private BigDecimal amountReceived; // Needed to calculate remaining amount

    @Schema(description = "Current status of the original receivable", example = "PARTIALLY_RECEIVED")
    private ReceivableStatus status; // Uses local enum copy

    @Schema(description = "Date the specific payment was received (relevant for statement)", example = "2024-06-05", nullable = true)
    private LocalDate receivedDate; // Date when THIS payment was received (for statement)
}