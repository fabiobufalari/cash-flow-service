package com.bufalari.cashflow.dto;

import com.bufalari.cashflow.enums.EntryType;
import io.swagger.v3.oas.annotations.media.Schema; // Import Schema
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * DTO for manual cash entries (credits/debits). Uses UUID for ID.
 * DTO para lançamentos manuais de caixa (créditos/débitos). Usa UUID para ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualCashEntryDTO {

    @Schema(description = "Unique identifier (UUID) of the manual entry", example = "fedcba98-7654-3210-fedc-ba9876543210", readOnly = true)
    private UUID id; // <<<--- Changed to UUID (Read-only in response)

    @NotNull(message = "Entry date cannot be null / Data do lançamento não pode ser nula")
    @Schema(description = "Date the transaction occurred or should be accounted for", example = "2024-05-03")
    private LocalDate entryDate;

    @NotNull(message = "Amount cannot be null / Valor não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive / Valor deve ser positivo")
    @Schema(description = "The amount of the transaction (always positive)", example = "150.00")
    private BigDecimal amount;

    @NotNull(message = "Entry type cannot be null / Tipo do lançamento não pode ser nulo")
    @Schema(description = "Type of the entry: CREDIT (inflow) or DEBIT (outflow)", example = "DEBIT")
    private EntryType type; // CREDIT or DEBIT

    @NotBlank(message = "Description cannot be blank / Descrição não pode ser vazia")
    @Size(max = 300)
    @Schema(description = "Description of the manual entry", example = "Office Cleaning Services")
    private String description;

    @Schema(description = "Optional ID of the project this entry relates to", example = "101", nullable = true)
    private Long projectId; // Optional, remains Long

    @Schema(description = "Optional ID of the cost center this entry relates to", example = "202", nullable = true)
    private Long costCenterId; // Optional, remains Long

    @Schema(description = "List of document references (IDs/URLs) associated with this entry", nullable = true, readOnly = true)
    private List<String> documentReferences; // Optional
}