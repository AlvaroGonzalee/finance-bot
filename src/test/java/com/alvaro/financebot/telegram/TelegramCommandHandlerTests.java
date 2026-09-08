package com.alvaro.financebot.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.alvaro.financebot.entity.FinancialOperationEntity;
import com.alvaro.financebot.entity.OperationType;
import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.service.OperationService;
import java.math.BigDecimal;
import java.time.Instant;
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
}
