package com.alvaro.financebot.dto.operation;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Resumen de las operaciones de un mes concreto.
 */
public record MonthlySummaryDTO(
    YearMonth month,
    List<FinancialOperationDTO> operations,
    BigDecimal incomeTotal,
    BigDecimal expenseTotal,
    BigDecimal balance
) {
}
