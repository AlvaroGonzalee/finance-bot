package com.alvaro.financebot.service;

import com.alvaro.financebot.dto.operation.RegisterOperationRequestDTO;
import com.alvaro.financebot.entity.FinancialOperationEntity;
import com.alvaro.financebot.repository.FinancialOperationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contiene la lógica de negocio para registrar operaciones financieras.
 */
@Service
@RequiredArgsConstructor
public class OperationService {

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

  private void validate(RegisterOperationRequestDTO request) {
    if (request == null) {
      throw new IllegalArgumentException("LA OPERACIÓN ES OBLIGATORIA.");
    }
    if (request.telegramUserId() == null) {
      throw new IllegalArgumentException("EL USUARIO DE TELEGRAM ES OBLIGATORIO.");
    }
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
}
