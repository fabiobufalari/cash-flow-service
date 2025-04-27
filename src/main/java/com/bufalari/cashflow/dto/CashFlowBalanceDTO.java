// Path: src/main/java/com/bufalari/cashflow/dto/CashFlowBalanceDTO.java
package com.bufalari.cashflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowBalanceDTO {
    private LocalDate date;
    private BigDecimal balance;
}