package com.alvaro.financebot.service;

import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.dto.operation.FinancialOperationDTO;
import com.alvaro.financebot.dto.operation.MonthlyHistoryDTO;
import com.alvaro.financebot.dto.operation.MonthlySummaryDTO;
import com.alvaro.financebot.entity.FinancialOperationEntity;
import com.alvaro.financebot.entity.OperationType;
import com.alvaro.financebot.repository.FinancialOperationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contiene la lógica de negocio para registrar operaciones financieras.
 */
@Service
@RequiredArgsConstructor
public class OperationService {

  private static final ZoneId BOT_ZONE_ID = ZoneId.of("Europe/Madrid");

  private final FinancialOperationRepository repository;

  @Transactional
  public FinancialOperationEntity register(RegisterOperationRequestDTO request) {
    validate(request);

    FinancialOperationEntity operation = new FinancialOperationEntity(
        null,
        request.telegramUserId(),
        request.concept().trim(),
        request.amount(),
        request.type(),
        Instant.now()
    );
    return repository.save(operation);
  }

  public MonthlySummaryDTO getCurrentMonthSummary(Long telegramUserId) {
    validateTelegramUserId(telegramUserId);

    YearMonth currentMonth = YearMonth.now(BOT_ZONE_ID);
    Instant startOfMonth = currentMonth.atDay(1).atStartOfDay(BOT_ZONE_ID).toInstant();
    Instant startOfNextMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay(BOT_ZONE_ID).toInstant();
    List<FinancialOperationEntity> operations = repository
        .findByTelegramUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            telegramUserId, startOfMonth, startOfNextMonth);

    return createMonthlySummary(currentMonth, operations);
  }

  public List<MonthlyHistoryDTO> getMonthlyHistory(Long telegramUserId) {
    validateTelegramUserId(telegramUserId);

    Map<YearMonth, List<FinancialOperationEntity>> operationsByMonth = new LinkedHashMap<>();
    for (FinancialOperationEntity operation : repository.findByTelegramUserIdOrderByCreatedAtAsc(telegramUserId)) {
      YearMonth month = YearMonth.from(operation.getCreatedAt().atZone(BOT_ZONE_ID));
      operationsByMonth.computeIfAbsent(month, ignored -> new ArrayList<>()).add(operation);
    }

    BigDecimal accumulatedSavings = BigDecimal.ZERO;
    List<MonthlyHistoryDTO> history = new ArrayList<>();
    for (Map.Entry<YearMonth, List<FinancialOperationEntity>> entry : operationsByMonth.entrySet()) {
      MonthlySummaryDTO summary = createMonthlySummary(entry.getKey(), entry.getValue());
      accumulatedSavings = accumulatedSavings.add(summary.balance());
      history.add(new MonthlyHistoryDTO(
          summary.month(),
          summary.incomeTotal(),
          summary.expenseTotal(),
          summary.balance(),
          accumulatedSavings
      ));
    }
    return history;
  }

  @Transactional
  public FinancialOperationEntity deleteLastOperation(Long telegramUserId) {
    validateTelegramUserId(telegramUserId);

    FinancialOperationEntity operation = repository
        .findTopByTelegramUserIdOrderByCreatedAtDescIdDesc(telegramUserId)
        .orElseThrow(() -> new IllegalArgumentException("NO HAY OPERACIONES PARA ELIMINAR."));
    repository.delete(operation);
    return operation;
  }

  private MonthlySummaryDTO createMonthlySummary(
      YearMonth month, List<FinancialOperationEntity> operations) {
    BigDecimal incomeTotal = totalForType(operations, OperationType.INCOME);
    BigDecimal expenseTotal = totalForType(operations, OperationType.EXPENSE);
    List<FinancialOperationDTO> operationDTOs = operations.stream()
        .map(this::toDTO)
        .toList();

    return new MonthlySummaryDTO(
        month,
        operationDTOs,
        incomeTotal,
        expenseTotal,
        incomeTotal.subtract(expenseTotal)
    );
  }

  private BigDecimal totalForType(List<FinancialOperationEntity> operations, OperationType type) {
    return operations.stream()
        .filter(operation -> operation.getType() == type)
        .map(FinancialOperationEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private FinancialOperationDTO toDTO(FinancialOperationEntity operation) {
    return new FinancialOperationDTO(
        operation.getId(),
        operation.getConcept(),
        operation.getAmount(),
        operation.getType(),
        operation.getCreatedAt()
    );
  }

  private void validate(RegisterOperationRequestDTO request) {
    if (request == null) {
      throw new IllegalArgumentException("LA OPERACIÓN ES OBLIGATORIA.");
    }
    validateTelegramUserId(request.telegramUserId());
    if (request.concept() == null || request.concept().isBlank()) {
      throw new IllegalArgumentException("EL CONCEPTO ES OBLIGATORIO.");
    }
    if (request.concept().trim().length() > 100) {
      throw new IllegalArgumentException("EL CONCEPTO NO PUEDE SUPERAR 100 CARACTERES.");
    }
    if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("EL IMPORTE DEBE SER MAYOR QUE CERO.");
    }
    if (request.amount().stripTrailingZeros().scale() > 2) {
      throw new IllegalArgumentException("EL IMPORTE NO PUEDE TENER MÁS DE DOS DECIMALES.");
    }
    if (request.type() == null) {
      throw new IllegalArgumentException("EL TIPO DE OPERACIÓN ES OBLIGATORIO.");
    }
  }

  private void validateTelegramUserId(Long telegramUserId) {
    if (telegramUserId == null) {
      throw new IllegalArgumentException("EL USUARIO DE TELEGRAM ES OBLIGATORIO.");
    }
  }
}
