package com.bufalari.cashflow.dto;

import com.bufalari.cashflow.enums.PayableStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simplified DTO representing payable data needed for cash flow calculation.
 * DTO simplificado representando dados de contas a pagar necessários para cálculo de fluxo de caixa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayableSummaryDTO {
    private Long id;
    private LocalDate dueDate;
    private BigDecimal amountDue;
    private BigDecimal amountPaid; // Needed to calculate remaining amount
    private PayableStatus status;
    private LocalDate paymentDate; // Date when it was actually paid
}