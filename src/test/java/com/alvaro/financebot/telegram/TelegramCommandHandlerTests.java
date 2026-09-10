package com.alvaro.financebot.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.alvaro.financebot.entity.FinancialOperationEntity;
import com.alvaro.financebot.entity.OperationType;
import com.alvaro.financebot.dto.operation.FinancialOperationDTO;
import com.alvaro.financebot.dto.operation.MonthlyHistoryDTO;
import com.alvaro.financebot.dto.operation.MonthlySummaryDTO;
import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.service.OperationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramCommandHandlerTests {

  @Mock
  private OperationService operationService;

  @InjectMocks
  private TelegramCommandHandler commandHandler;

  @Test
  void registersAnExpenseFromATelegramCommand() {
    when(operationService.register(any())).thenReturn(new FinancialOperationEntity(
        1L, 12345L, "Cena", new BigDecimal("24.50"), OperationType.EXPENSE, Instant.now()));

    String response = commandHandler.handle(12345L, "/expense 24,50 cena");

    ArgumentCaptor<RegisterOperationRequestDTO> commandCaptor =
        ArgumentCaptor.forClass(RegisterOperationRequestDTO.class);
    verify(operationService).register(commandCaptor.capture());
    assertThat(commandCaptor.getValue()).isEqualTo(new RegisterOperationRequestDTO(
        12345L, "CENA", new BigDecimal("24.50"), OperationType.EXPENSE));
    assertThat(response).isEqualTo("GASTO DE 24,50 € REGISTRADO: CENA");
  }

  @Test
  void returnsAnIncorrectMessageForAnInvalidStructure() {
    String response = commandHandler.handle(12345L, "/expense 20");

    assertThat(response).isEqualTo("MENSAJE INCORRECTO. ESCRIBE /HELP.");
    verifyNoInteractions(operationService);
  }

  @Test
  void returnsAnIncorrectMessageForAnInvalidAmount() {
    String response = commandHandler.handle(12345L, "/income veinte nomina");

    assertThat(response).isEqualTo("MENSAJE INCORRECTO. ESCRIBE /HELP.");
    verifyNoInteractions(operationService);
  }

  @Test
  void returnsTheCurrentMonthOperationsAndTotals() {
    when(operationService.getCurrentMonthSummary(12345L)).thenReturn(new MonthlySummaryDTO(
        YearMonth.of(2026, 9),
        List.of(new FinancialOperationDTO(
            8L, "AGUA", new BigDecimal("100.00"), OperationType.EXPENSE, Instant.now())),
        BigDecimal.ZERO,
        new BigDecimal("100.00"),
        new BigDecimal("-100.00")
    ));

    String response = commandHandler.handle(12345L, "/monthly");

    assertThat(response).contains("OPERACIONES DE SEPTIEMBRE 2026:");
    assertThat(response).contains("#8 | GASTO | AGUA | 100,00 €");
    assertThat(response).contains("INGRESOS: 0,00 €");
    assertThat(response).contains("GASTOS: 100,00 €");
    assertThat(response).contains("BALANCE: -100,00 €");
  }

  @Test
  void returnsMonthlyHistoryWithAccumulatedSavings() {
    when(operationService.getMonthlyHistory(12345L)).thenReturn(List.of(new MonthlyHistoryDTO(
        YearMonth.of(2026, 9),
        new BigDecimal("1600.00"),
        new BigDecimal("100.00"),
        new BigDecimal("1500.00"),
        new BigDecimal("1500.00")
    )));

    String response = commandHandler.handle(12345L, "/historico");

    assertThat(response).contains("HISTÓRICO FINANCIERO:");
    assertThat(response).contains("SEPTIEMBRE 2026 | INGRESOS: 1600,00 €");
    assertThat(response).contains("AHORRO ACUMULADO: 1500,00 €");
  }

  @Test
  void deletesTheLastOperationForTheTelegramUser() {
    when(operationService.deleteLastOperation(12345L)).thenReturn(new FinancialOperationEntity(
        8L, 12345L, "AGUA", new BigDecimal("100.00"), OperationType.EXPENSE, Instant.now()));

    String response = commandHandler.handle(12345L, "/eliminar");

    assertThat(response).isEqualTo("ÚLTIMA OPERACIÓN ELIMINADA: GASTO DE 100,00 € - AGUA");
    verify(operationService).deleteLastOperation(12345L);
  }
}
