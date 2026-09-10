package com.alvaro.financebot.repository;

import com.alvaro.financebot.entity.FinancialOperationEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialOperationRepository extends JpaRepository<FinancialOperationEntity, Long> {

  List<FinancialOperationEntity> findByTelegramUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
      Long telegramUserId, Instant startOfMonth, Instant startOfNextMonth);

  List<FinancialOperationEntity> findByTelegramUserIdOrderByCreatedAtAsc(Long telegramUserId);

  Optional<FinancialOperationEntity> findTopByTelegramUserIdOrderByCreatedAtDescIdDesc(Long telegramUserId);
}
