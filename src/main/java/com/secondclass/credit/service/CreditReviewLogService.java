package com.secondclass.credit.service;

import com.secondclass.credit.domain.entity.CreditReviewLog;
import com.secondclass.credit.domain.enums.CreditReviewAction;
import com.secondclass.credit.repository.CreditReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditReviewLogService {

    private final CreditReviewLogRepository creditReviewLogRepository;

    public void saveLog(Long recordId,
                        CreditReviewAction action,
                        String operatorRole,
                        String remark,
                        boolean success,
                        String detail) {
        CreditReviewLog log = new CreditReviewLog();
        log.setRecordId(recordId);
        log.setAction(action);
        log.setOperatorRole(normalizeRole(operatorRole));
        log.setRemark(remark);
        log.setSuccess(success);
        log.setDetail(detail);
        creditReviewLogRepository.save(log);
    }

    private String normalizeRole(String operatorRole) {
        if (operatorRole == null || operatorRole.isBlank()) {
            return "UNKNOWN";
        }
        return operatorRole.trim().toUpperCase();
    }
}
