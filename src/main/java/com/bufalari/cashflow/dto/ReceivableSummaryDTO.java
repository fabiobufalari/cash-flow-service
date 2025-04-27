// Path: src/main/java/com/bufalari/cashflow/dto/ReceivableSummaryDTO.java
package com.bufalari.cashflow.dto;


import com.bufalari.cashflow.enums.ReceivableStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simplified DTO representing receivable data needed for cash flow calculation.
 * DTO simplificado representando dados de contas a receber necessários para cálculo de fluxo de caixa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableSummaryDTO {
    private Long id;
    private LocalDate dueDate;
    private BigDecimal amountExpected;
    private BigDecimal amountReceived; // Needed to calculate remaining amount
    private ReceivableStatus status;
     private LocalDate receivedDate; // Date when it was actually received
}