package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.PageResult;
import com.secondclass.credit.domain.entity.CreditReviewLog;
import com.secondclass.credit.domain.enums.CreditReviewAction;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.CreditReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public PageResult<CreditReviewLog> listLogsPage(Long recordId,
                                                    CreditReviewAction action,
                                                    Boolean success,
                                                    int page,
                                                    int size) {
        validatePageParams(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CreditReviewLog> logPage = creditReviewLogRepository.search(recordId, action, success, pageable);
        return PageResult.<CreditReviewLog>builder()
                .page(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .content(logPage.getContent())
                .build();
    }

    private String normalizeRole(String operatorRole) {
        if (operatorRole == null || operatorRole.isBlank()) {
            return "UNKNOWN";
        }
        return operatorRole.trim().toUpperCase();
    }

    private void validatePageParams(int page, int size) {
        if (page < 0) {
            throw new BusinessException("page 不能小于 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("size 必须在 1 到 100 之间");
        }
    }
}
