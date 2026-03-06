package com.secondclass.credit.repository;

import com.secondclass.credit.domain.entity.CreditReviewLog;
import com.secondclass.credit.domain.enums.CreditReviewAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditReviewLogRepository extends JpaRepository<CreditReviewLog, Long> {

    @Query("""
            SELECT log
            FROM CreditReviewLog log
            WHERE (:recordId IS NULL OR log.recordId = :recordId)
              AND (:action IS NULL OR log.action = :action)
              AND (:success IS NULL OR log.success = :success)
            ORDER BY log.createdAt DESC
            """)
    Page<CreditReviewLog> search(@Param("recordId") Long recordId,
                                 @Param("action") CreditReviewAction action,
                                 @Param("success") Boolean success,
                                 Pageable pageable);
}
