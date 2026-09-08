package com.alvaro.financebot.telegram;

import com.alvaro.financebot.entity.OperationType;
import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.service.OperationService;
import java.math.BigDecimal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Traduce los mensajes de texto recibidos desde Telegram a operaciones financieras.
 *
 * <p>Interpreta los comandos {@code /EXPENSE} y {@code /INCOME}, extrae el importe y el concepto,
 * delega el registro en {@link OperationService} y devuelve el texto que se enviará como
 * respuesta al usuario.</p>
 *
 * <p>No se comunica directamente ni con la API de Telegram ni con la base de datos. El componente
 * de long polling le proporcionará el identificador del usuario y el texto del mensaje.</p>
 */
@Component
@RequiredArgsConstructor
public class TelegramCommandHandler {

  private static final String INCORRECT_MESSAGE = "MENSAJE INCORRECTO. ESCRIBE /HELP.";
  private static final String HELP_MESSAGE = """
      COMANDOS DISPONIBLES:

      /GASTO /EXPENSE - AÑADE UN NUEVO GASTO. EJ: /GASTO CENAAMIGOS 25
      /INGRESO /INCOME - AÑADE UN NUEVO INGRESO. EJ: /INGRESO SUELDO 1600
      /MENSUAL /MONTHLY - MUESTRA TODAS LAS OPERACIONES DEL MES ACTUAL Y SUS TOTALES.
      /HISTORICO /HISTORY - MUESTRA EL RESULTADO FINAL DE CADA MES Y EL AHORRO ACUMULADO.
      /ELIMINAR /DELETE - ELIMINA UNA OPERACIÓN MEDIANTE SU ID. EJ: /ELIMINAR 34
      /AYUDA /HELP - MUESTRA LOS COMANDOS DISPONIBLES Y CÓMO UTILIZARLOS.
      """;

  private final OperationService operationService;

  public String handle(Long telegramUserId, String text) {
    if (telegramUserId == null || text == null || text.isBlank()) {
      return INCORRECT_MESSAGE;
    }

    String normalizedText = text.trim().toUpperCase(Locale.ROOT);
    if (normalizedText.equals("/HELP") || normalizedText.equals("/AYUDA")) {
      return HELP_MESSAGE;
    }

    String[] parts = normalizedText.split("\\s+", 3);
    if (parts.length < 3) {
      return INCORRECT_MESSAGE;
    }

    OperationType type = operationType(parts[0]);
    if (type == null) {
      return INCORRECT_MESSAGE;
    }

    BigDecimal amount;
    try {
      amount = new BigDecimal(parts[1].replace(',', '.'));
    } catch (NumberFormatException exception) {
      return INCORRECT_MESSAGE;
    }

    try {
      operationService.register(new RegisterOperationRequestDTO(telegramUserId, parts[2], amount, type));
    } catch (IllegalArgumentException exception) {
      return "NO HE PODIDO REGISTRAR LA OPERACIÓN: " + exception.getMessage();
    }

    String typeLabel = type == OperationType.EXPENSE ? "GASTO" : "INGRESO";
    return "%s DE %s € REGISTRADO: %s".formatted(
        typeLabel, amount.setScale(2).toPlainString().replace('.', ','), parts[2].trim());
  }

  private OperationType operationType(String command) {
    String normalizedCommand = command.replaceFirst("@[^\\s]+$", "");
    return switch (normalizedCommand) {
      case "/EXPENSE", "/GASTO" -> OperationType.EXPENSE;
      case "/INCOME", "/INGRESO" -> OperationType.INCOME;
      default -> null;
    };
  }
}
