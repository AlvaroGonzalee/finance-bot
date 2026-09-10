package com.alvaro.financebot.dto;

import com.alvaro.financebot.entity.OperationType;
import java.math.BigDecimal;

/**
 * Datos necesarios para registrar una operación financiera.
 *
 * @param telegramUserId identificador del usuario que ha enviado el mensaje
 * @param concept descripción de la operación
 * @param amount importe de la operación
 * @param type tipo de operación: gasto o ingreso
 */
public record RegisterOperationRequestDTO(
    Long telegramUserId,
    String concept,
    BigDecimal amount,
    OperationType type
) {
}
