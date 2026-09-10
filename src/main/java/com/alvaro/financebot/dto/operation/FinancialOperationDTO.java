package com.alvaro.financebot.dto.operation;

import com.alvaro.financebot.entity.OperationType;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Información de una operación financiera para mostrar al usuario.
 */
public record FinancialOperationDTO(
    Long id,
    String concept,
    BigDecimal amount,
    OperationType type,
    Instant createdAt
) {
}
