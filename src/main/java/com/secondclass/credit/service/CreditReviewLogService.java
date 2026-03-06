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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    int page,
                                                    int size) {
        validatePageParams(page, size);
        validateDateRange(startDate, endDate);

        Pageable pageable = PageRequest.of(page, size);
        Page<CreditReviewLog> logPage = creditReviewLogRepository.search(
                recordId,
                action,
                success,
                resolveStartTime(startDate),
                resolveEndTime(endDate),
                pageable
        );
        return PageResult.<CreditReviewLog>builder()
                .page(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .content(logPage.getContent())
                .build();
    }

    public String exportLogsCsv(Long recordId,
                                CreditReviewAction action,
                                Boolean success,
                                LocalDate startDate,
                                LocalDate endDate,
                                int limit) {
        validateExportLimit(limit);
        validateDateRange(startDate, endDate);

        Pageable pageable = PageRequest.of(0, limit);
        Page<CreditReviewLog> logPage = creditReviewLogRepository.search(
                recordId,
                action,
                success,
                resolveStartTime(startDate),
                resolveEndTime(endDate),
                pageable
        );

        StringBuilder builder = new StringBuilder("id,recordId,action,operatorRole,success,remark,detail,createdAt\n");
        for (CreditReviewLog log : logPage.getContent()) {
            builder.append(log.getId() == null ? "" : log.getId()).append(',')
                    .append(log.getRecordId() == null ? "" : log.getRecordId()).append(',')
                    .append(log.getAction() == null ? "" : log.getAction().name()).append(',')
                    .append(toCsvCell(log.getOperatorRole())).append(',')
                    .append(log.getSuccess() == null ? "" : log.getSuccess()).append(',')
                    .append(toCsvCell(log.getRemark())).append(',')
                    .append(toCsvCell(log.getDetail())).append(',')
                    .append(log.getCreatedAt() == null ? "" : log.getCreatedAt())
                    .append('\n');
        }
        return builder.toString();
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

    private void validateExportLimit(int limit) {
        if (limit <= 0 || limit > 5000) {
            throw new BusinessException("limit 必须在 1 到 5000 之间");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("startDate 不能晚于 endDate");
        }
    }

    private LocalDateTime resolveStartTime(LocalDate startDate) {
        if (startDate == null) {
            return null;
        }
        return startDate.atStartOfDay();
    }

    private LocalDateTime resolveEndTime(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        return endDate.atTime(LocalTime.MAX);
    }

    private String toCsvCell(String value) {
        if (value == null) {
            return "";
        }
        if (!value.contains(",") && !value.contains("\"") && !value.contains("\n")) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
