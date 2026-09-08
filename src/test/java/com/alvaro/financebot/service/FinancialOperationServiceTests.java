package com.alvaro.financebot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.alvaro.financebot.entity.OperationType;
import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.repository.FinancialOperationRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinancialOperationServiceTests {

  @Mock
  private FinancialOperationRepository repository;

  @InjectMocks
  private OperationService service;

  @Test
  void registersAnExpenseForTheTelegramUser() {
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var operation = service.register(new RegisterOperationRequestDTO(
        12345L, "  Cena  ", new BigDecimal("24.50"), OperationType.EXPENSE));

    assertThat(operation.getTelegramUserId()).isEqualTo(12345L);
    assertThat(operation.getConcept()).isEqualTo("Cena");
    assertThat(operation.getType()).isEqualTo(OperationType.EXPENSE);
    assertThat(operation.getCreatedAt()).isNotNull();
    verify(repository).save(operation);
  }

  @Test
  void rejectsAZeroOrNegativeAmount() {
    var command = new RegisterOperationRequestDTO(
        12345L, "Cena", BigDecimal.ZERO, OperationType.EXPENSE);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.register(command))
        .withMessage("EL IMPORTE DEBE SER MAYOR QUE CERO.");

    verifyNoInteractions(repository);
  }

  @Test
  void rejectsAnAmountWithMoreThanTwoDecimals() {
    var command = new RegisterOperationRequestDTO(
        12345L, "Cena", new BigDecimal("24.567"), OperationType.EXPENSE);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.register(command))
        .withMessage("EL IMPORTE NO PUEDE TENER MÁS DE DOS DECIMALES.");

    verifyNoInteractions(repository);
  }
}
