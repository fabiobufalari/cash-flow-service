// Path: src/main/java/com/bufalari/cashflow/dto/CashFlowForecastDTO.java
package com.bufalari.cashflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map; // Using Map for daily forecast

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowForecastDTO {
    private LocalDate forecastStartDate;
    private BigDecimal startingBalance;
    private Map<LocalDate, BigDecimal> dailyProjectedBalance; // Date -> Projected Balance
    // Optionally add lists of expected inflows/outflows per day
    // Opcionalmente adicione listas de entradas/saídas esperadas por dia
}