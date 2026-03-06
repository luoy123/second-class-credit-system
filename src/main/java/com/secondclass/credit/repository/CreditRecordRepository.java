package com.secondclass.credit.repository;

import com.secondclass.credit.domain.entity.CreditRecord;
import com.secondclass.credit.domain.enums.CreditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {

    List<CreditRecord> findByStatus(CreditStatus status);

    List<CreditRecord> findByStudentIdAndStatus(Long studentId, CreditStatus status);

    List<CreditRecord> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    Page<CreditRecord> findByStudentId(Long studentId, Pageable pageable);

    Page<CreditRecord> findByStudentIdAndStatus(Long studentId, CreditStatus status, Pageable pageable);

    Page<CreditRecord> findByStatus(CreditStatus status, Pageable pageable);

    @Query("""
            SELECT record
            FROM CreditRecord record
            WHERE record.studentId = :studentId
              AND (:status IS NULL OR record.status = :status)
              AND (:category IS NULL OR record.category = :category)
              AND (:startTime IS NULL OR record.createdAt >= :startTime)
              AND (:endTime IS NULL OR record.createdAt <= :endTime)
            ORDER BY record.createdAt DESC
            """)
    Page<CreditRecord> searchStudentRecords(@Param("studentId") Long studentId,
                                            @Param("status") CreditStatus status,
                                            @Param("category") String category,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            Pageable pageable);
}
