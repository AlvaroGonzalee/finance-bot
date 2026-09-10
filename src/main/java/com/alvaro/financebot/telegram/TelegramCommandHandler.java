package com.alvaro.financebot.telegram;

import com.alvaro.financebot.dto.operation.FinancialOperationDTO;
import com.alvaro.financebot.dto.operation.MonthlyHistoryDTO;
import com.alvaro.financebot.dto.operation.MonthlySummaryDTO;
import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.entity.FinancialOperationEntity;
import com.alvaro.financebot.entity.OperationType;
import com.alvaro.financebot.service.OperationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Traduce los mensajes de texto recibidos desde Telegram a operaciones financieras.
 *
 * <p>Interpreta los comandos recibidos, delega la lógica financiera en {@link OperationService}
 * y devuelve el texto que se enviará como respuesta al usuario.</p>
 *
 * <p>No se comunica directamente ni con la API de Telegram ni con la base de datos. El componente
 * de long polling le proporcionará el identificador del usuario y el texto del mensaje.</p>
 */
@Component
@RequiredArgsConstructor
public class TelegramCommandHandler {

  private static final String INCORRECT_MESSAGE = "MENSAJE INCORRECTO. ESCRIBE /HELP.";
  private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es-ES");
  private static final String HELP_MESSAGE = """
      COMANDOS DISPONIBLES:

      /GASTO /EXPENSE - AÑADE UN NUEVO GASTO. EJ: /GASTO 25 CENAAMIGOS
      /INGRESO /INCOME - AÑADE UN NUEVO INGRESO. EJ: /INGRESO 1600 SUELDO
      /MENSUAL /MONTHLY - MUESTRA TODAS LAS OPERACIONES DEL MES ACTUAL Y SUS TOTALES.
      /HISTORICO /HISTORY - MUESTRA EL RESULTADO FINAL DE CADA MES Y EL AHORRO ACUMULADO.
      /ELIMINAR /DELETE - ELIMINA LA ÚLTIMA OPERACIÓN REGISTRADA.
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
    if (normalizedText.equals("/MONTHLY") || normalizedText.equals("/MENSUAL")) {
      return getCurrentMonthSummary(telegramUserId);
    }
    if (normalizedText.equals("/HISTORY") || normalizedText.equals("/HISTORICO")) {
      return getMonthlyHistory(telegramUserId);
    }
    if (normalizedText.equals("/DELETE") || normalizedText.equals("/ELIMINAR")) {
      return deleteLastOperation(telegramUserId);
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
        typeLabel, formatAmount(amount), parts[2].trim());
  }

  private String getCurrentMonthSummary(Long telegramUserId) {
    MonthlySummaryDTO summary = operationService.getCurrentMonthSummary(telegramUserId);
    if (summary.operations().isEmpty()) {
      return "NO HAY OPERACIONES REGISTRADAS EN " + formatMonth(summary.month()) + ".";
    }

    StringBuilder response = new StringBuilder("OPERACIONES DE ")
        .append(formatMonth(summary.month()))
        .append(":\n");
    for (FinancialOperationDTO operation : summary.operations()) {
      String typeLabel = operation.type() == OperationType.EXPENSE ? "GASTO" : "INGRESO";
      response.append("#")
          .append(operation.id())
          .append(" | ")
          .append(typeLabel)
          .append(" | ")
          .append(operation.concept())
          .append(" | ")
          .append(formatAmount(operation.amount()))
          .append(" €\n");
    }
    return response.append("\nINGRESOS: ")
        .append(formatAmount(summary.incomeTotal()))
        .append(" €\nGASTOS: ")
        .append(formatAmount(summary.expenseTotal()))
        .append(" €\nBALANCE: ")
        .append(formatAmount(summary.balance()))
        .append(" €")
        .toString();
  }

  private String getMonthlyHistory(Long telegramUserId) {
    List<MonthlyHistoryDTO> history = operationService.getMonthlyHistory(telegramUserId);
    if (history.isEmpty()) {
      return "NO HAY OPERACIONES EN EL HISTÓRICO.";
    }

    StringBuilder response = new StringBuilder("HISTÓRICO FINANCIERO:\n");
    for (MonthlyHistoryDTO month : history) {
      response.append(formatMonth(month.month()))
          .append(" | INGRESOS: ").append(formatAmount(month.incomeTotal())).append(" €")
          .append(" | GASTOS: ").append(formatAmount(month.expenseTotal())).append(" €")
          .append(" | BALANCE: ").append(formatAmount(month.balance())).append(" €")
          .append(" | AHORRO ACUMULADO: ").append(formatAmount(month.accumulatedSavings()))
          .append(" €\n");
    }
    return response.toString().trim();
  }

  private String deleteLastOperation(Long telegramUserId) {
    try {
      FinancialOperationEntity operation = operationService.deleteLastOperation(telegramUserId);
      String typeLabel = operation.getType() == OperationType.EXPENSE ? "GASTO" : "INGRESO";
      return "ÚLTIMA OPERACIÓN ELIMINADA: %s DE %s € - %s".formatted(
          typeLabel, formatAmount(operation.getAmount()), operation.getConcept());
    } catch (IllegalArgumentException exception) {
      return "NO HE PODIDO ELIMINAR LA OPERACIÓN: " + exception.getMessage();
    }
  }

  private String formatMonth(YearMonth month) {
    return "%s %d".formatted(
        month.getMonth().getDisplayName(TextStyle.FULL, SPANISH_LOCALE)
            .toUpperCase(Locale.ROOT),
        month.getYear());
  }

  private String formatAmount(BigDecimal amount) {
    return amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString().replace('.', ',');
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
