// Path: src/main/java/com/bufalari/cashflow/dto/CashFlowItemDTO.java
package com.bufalari.cashflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowItemDTO {
    private LocalDate date; // Date the flow occurred or is expected
    private String description;
    private BigDecimal amount;
    private String type; // e.g., "RECEIVABLE", "PAYABLE", "MANUAL_CREDIT", "MANUAL_DEBIT"
    private Long relatedId; // ID of the receivable, payable, or manual entry
}