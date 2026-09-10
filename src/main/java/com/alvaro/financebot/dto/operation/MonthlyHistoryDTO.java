package com.alvaro.financebot.dto.operation;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Resultado financiero de un mes y el ahorro acumulado hasta ese momento.
 */
public record MonthlyHistoryDTO(
    YearMonth month,
    BigDecimal incomeTotal,
    BigDecimal expenseTotal,
    BigDecimal balance,
    BigDecimal accumulatedSavings
) {
}
