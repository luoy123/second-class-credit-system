package com.secondclass.credit.repository;

import com.secondclass.credit.domain.entity.CreditRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditRuleRepository extends JpaRepository<CreditRule, Long> {

    boolean existsByCategory(String category);

    Optional<CreditRule> findFirstByCategoryAndEnabledTrue(String category);
}
