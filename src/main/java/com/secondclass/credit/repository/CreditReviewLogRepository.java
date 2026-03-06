package com.secondclass.credit.repository;

import com.secondclass.credit.domain.entity.CreditReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditReviewLogRepository extends JpaRepository<CreditReviewLog, Long> {
}
