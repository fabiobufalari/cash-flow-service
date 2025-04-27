// Path: src/main/java/com/bufalari/cashflow/dto/CashFlowStatementDTO.java
package com.bufalari.cashflow.dto;

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
public class CashFlowStatementDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal openingBalance;
    private BigDecimal totalInflows;
    private BigDecimal totalOutflows;
    private BigDecimal netCashFlow;
    private BigDecimal closingBalance;
    private List<CashFlowItemDTO> inflowItems; // Detailed inflows
    private List<CashFlowItemDTO> outflowItems; // Detailed outflows
}