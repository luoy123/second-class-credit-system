package com.secondclass.credit.repository;

import com.secondclass.credit.domain.entity.CreditRecord;
import com.secondclass.credit.domain.enums.CreditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {

    List<CreditRecord> findByStatus(CreditStatus status);

    List<CreditRecord> findByStudentIdAndStatus(Long studentId, CreditStatus status);

    List<CreditRecord> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    Page<CreditRecord> findByStudentId(Long studentId, Pageable pageable);

    Page<CreditRecord> findByStudentIdAndStatus(Long studentId, CreditStatus status, Pageable pageable);

    Page<CreditRecord> findByStatus(CreditStatus status, Pageable pageable);
}
