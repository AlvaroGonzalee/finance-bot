package com.alvaro.financebot.repository;

import com.alvaro.financebot.entity.FinancialOperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialOperationRepository extends JpaRepository<FinancialOperationEntity, Long> {
}
