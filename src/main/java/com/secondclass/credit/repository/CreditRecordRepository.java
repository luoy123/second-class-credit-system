package com.secondclass.credit.repository;

import com.secondclass.credit.domain.entity.CreditRecord;
import com.secondclass.credit.domain.enums.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {

    List<CreditRecord> findByStudentIdAndStatus(Long studentId, CreditStatus status);

    List<CreditRecord> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
