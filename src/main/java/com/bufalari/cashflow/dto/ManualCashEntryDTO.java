// Path: src/main/java/com/bufalari/cashflow/dto/ManualCashEntryDTO.java
package com.bufalari.cashflow.dto;

import com.bufalari.cashflow.enums.EntryType;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualCashEntryDTO {

    private Long id; // Read-only

    @NotNull(message = "Entry date cannot be null / Data do lançamento não pode ser nula")
    private LocalDate entryDate;

    @NotNull(message = "Amount cannot be null / Valor não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive / Valor deve ser positivo")
    private BigDecimal amount;

    @NotNull(message = "Entry type cannot be null / Tipo do lançamento não pode ser nulo")
    private EntryType type; // CREDIT or DEBIT

    @NotBlank(message = "Description cannot be blank / Descrição não pode ser vazia")
    @Size(max = 300)
    private String description;

    private Long projectId; // Optional
    private Long costCenterId; // Optional
    private List<String> documentReferences; // Optional
}