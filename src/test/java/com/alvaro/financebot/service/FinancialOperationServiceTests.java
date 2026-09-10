package com.alvaro.financebot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.alvaro.financebot.entity.OperationType;
import com.alvaro.financebot.entity.FinancialOperationEntity;
import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.repository.FinancialOperationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

  @Test
  void calculatesTheCurrentMonthTotals() {
    when(repository.findByTelegramUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
        any(), any(), any())).thenReturn(List.of(
            new FinancialOperationEntity(
                1L, 12345L, "SUELDO", new BigDecimal("1600.00"), OperationType.INCOME, Instant.now()),
            new FinancialOperationEntity(
                2L, 12345L, "AGUA", new BigDecimal("100.00"), OperationType.EXPENSE, Instant.now())
        ));

    var summary = service.getCurrentMonthSummary(12345L);

    assertThat(summary.incomeTotal()).isEqualByComparingTo("1600.00");
    assertThat(summary.expenseTotal()).isEqualByComparingTo("100.00");
    assertThat(summary.balance()).isEqualByComparingTo("1500.00");
    assertThat(summary.operations()).hasSize(2);
  }

  @Test
  void calculatesTheMonthlyHistoryAndAccumulatedSavings() {
    when(repository.findByTelegramUserIdOrderByCreatedAtAsc(12345L)).thenReturn(List.of(
        new FinancialOperationEntity(
            1L, 12345L, "SUELDO", new BigDecimal("1000.00"), OperationType.INCOME,
            Instant.parse("2026-01-15T12:00:00Z")),
        new FinancialOperationEntity(
            2L, 12345L, "ALQUILER", new BigDecimal("300.00"), OperationType.EXPENSE,
            Instant.parse("2026-02-15T12:00:00Z"))
    ));

    var history = service.getMonthlyHistory(12345L);

    assertThat(history).hasSize(2);
    assertThat(history.get(0).balance()).isEqualByComparingTo("1000.00");
    assertThat(history.get(0).accumulatedSavings()).isEqualByComparingTo("1000.00");
    assertThat(history.get(1).balance()).isEqualByComparingTo("-300.00");
    assertThat(history.get(1).accumulatedSavings()).isEqualByComparingTo("700.00");
  }

  @Test
  void deletesOnlyTheLatestOperationForTheTelegramUser() {
    FinancialOperationEntity latestOperation = new FinancialOperationEntity(
        2L, 12345L, "AGUA", new BigDecimal("100.00"), OperationType.EXPENSE, Instant.now());
    when(repository.findTopByTelegramUserIdOrderByCreatedAtDescIdDesc(12345L))
        .thenReturn(Optional.of(latestOperation));

    var deletedOperation = service.deleteLastOperation(12345L);

    assertThat(deletedOperation).isSameAs(latestOperation);
    verify(repository).delete(latestOperation);
  }
}
