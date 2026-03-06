package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.CategoryCreditStatResponse;
import com.secondclass.credit.domain.dto.CreditBatchReviewResult;
import com.secondclass.credit.domain.dto.CreditApplyRequest;
import com.secondclass.credit.domain.dto.DimensionCreditStatResponse;
import com.secondclass.credit.domain.dto.MonthlyCreditStatResponse;
import com.secondclass.credit.domain.dto.PageResult;
import com.secondclass.credit.domain.dto.StudentCreditRankingResponse;
import com.secondclass.credit.domain.dto.CreditSummaryResponse;
import com.secondclass.credit.domain.entity.Activity;
import com.secondclass.credit.domain.entity.CreditRecord;
import com.secondclass.credit.domain.entity.CreditRule;
import com.secondclass.credit.domain.entity.Student;
import com.secondclass.credit.domain.enums.CreditReviewAction;
import com.secondclass.credit.domain.enums.CreditStatus;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.CreditRecordRepository;
import com.secondclass.credit.repository.CreditRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRecordRepository creditRecordRepository;
    private final CreditRuleRepository creditRuleRepository;
    private final StudentService studentService;
    private final ActivityService activityService;
    private final CreditReviewLogService creditReviewLogService;

    public CreditRecord apply(CreditApplyRequest request) {
        studentService.findById(request.getStudentId());
        Activity activity = activityService.findById(request.getActivityId());

        CreditRule creditRule = creditRuleRepository.findFirstByCategoryAndEnabledTrue(request.getCategory())
                .orElseThrow(() -> new BusinessException("学分规则不存在或未启用，分类=" + request.getCategory()));

        CreditRecord creditRecord = new CreditRecord();
        creditRecord.setStudentId(request.getStudentId());
        creditRecord.setActivityId(request.getActivityId());
        creditRecord.setCategory(request.getCategory());
        creditRecord.setCredit(resolveCredit(creditRule, activity));
        creditRecord.setStatus(CreditStatus.PENDING);
        creditRecord.setRemark(request.getRemark());
        return creditRecordRepository.save(creditRecord);
    }

    public CreditRecord approve(Long recordId, String reviewRemark, String operatorRole) {
        return updateRecordStatus(recordId, CreditStatus.APPROVED, reviewRemark, operatorRole);
    }

    public CreditRecord reject(Long recordId, String reviewRemark, String operatorRole) {
        return updateRecordStatus(recordId, CreditStatus.REJECTED, reviewRemark, operatorRole);
    }

    public CreditBatchReviewResult batchApprove(List<Long> recordIds, String reviewRemark, String operatorRole) {
        return batchUpdateStatus(recordIds, CreditStatus.APPROVED, reviewRemark, operatorRole);
    }

    public CreditBatchReviewResult batchReject(List<Long> recordIds, String reviewRemark, String operatorRole) {
        return batchUpdateStatus(recordIds, CreditStatus.REJECTED, reviewRemark, operatorRole);
    }

    public CreditSummaryResponse getStudentSummary(Long studentId) {
        Student student = studentService.findById(studentId);
        List<CreditRecord> approvedRecords = creditRecordRepository.findByStudentIdAndStatus(studentId, CreditStatus.APPROVED);

        BigDecimal totalCredit = approvedRecords.stream()
                .map(CreditRecord::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryCreditMap = approvedRecords.stream()
                .collect(Collectors.groupingBy(
                        CreditRecord::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, CreditRecord::getCredit, BigDecimal::add)
                ));

        List<CreditSummaryResponse.CategoryCreditItem> categoryItems = categoryCreditMap.entrySet().stream()
                .map(entry -> CreditSummaryResponse.CategoryCreditItem.builder()
                        .category(entry.getKey())
                        .credit(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(CreditSummaryResponse.CategoryCreditItem::getCategory))
                .toList();

        return CreditSummaryResponse.builder()
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .totalCredit(totalCredit)
                .categoryCredits(categoryItems)
                .build();
    }

    public List<CreditRecord> listStudentRecords(Long studentId) {
        studentService.findById(studentId);
        return creditRecordRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public PageResult<CreditRecord> listStudentRecordsPage(Long studentId, CreditStatus status, int page, int size) {
        studentService.findById(studentId);
        validatePageParams(page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CreditRecord> recordPage = status == null
                ? creditRecordRepository.findByStudentId(studentId, pageable)
                : creditRecordRepository.findByStudentIdAndStatus(studentId, status, pageable);

        return buildPageResult(recordPage);
    }

    public PageResult<CreditRecord> listPendingRecordsPage(int page, int size) {
        validatePageParams(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CreditRecord> recordPage = creditRecordRepository.findByStatus(CreditStatus.PENDING, pageable);
        return buildPageResult(recordPage);
    }

    public List<CategoryCreditStatResponse> getCategoryStatistics() {
        List<CreditRecord> approvedRecords = creditRecordRepository.findByStatus(CreditStatus.APPROVED);
        Map<String, BigDecimal> categoryCreditMap = approvedRecords.stream()
                .collect(Collectors.groupingBy(
                        CreditRecord::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, CreditRecord::getCredit, BigDecimal::add)
                ));
        return categoryCreditMap.entrySet().stream()
                .map(entry -> CategoryCreditStatResponse.builder()
                        .category(entry.getKey())
                        .totalCredit(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(CategoryCreditStatResponse::getCategory))
                .toList();
    }

    public List<DimensionCreditStatResponse> getMajorStatistics() {
        return getDimensionStatistics(Student::getMajor);
    }

    public List<DimensionCreditStatResponse> getGradeStatistics() {
        return getDimensionStatistics(Student::getGrade);
    }

    public List<MonthlyCreditStatResponse> getMonthlyStatistics(int year) {
        List<CreditRecord> approvedRecords = creditRecordRepository.findByStatus(CreditStatus.APPROVED);
        Map<Integer, BigDecimal> monthCreditMap = approvedRecords.stream()
                .filter(record -> record.getCreatedAt() != null && record.getCreatedAt().getYear() == year)
                .collect(Collectors.groupingBy(
                        record -> record.getCreatedAt().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, CreditRecord::getCredit, BigDecimal::add)
                ));
        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> MonthlyCreditStatResponse.builder()
                        .month(month)
                        .totalCredit(monthCreditMap.getOrDefault(month, BigDecimal.ZERO))
                        .build())
                .toList();
    }

    public List<StudentCreditRankingResponse> getStudentRanking(int topN) {
        if (topN <= 0) {
            throw new BusinessException("topN 必须大于 0");
        }

        List<CreditRecord> approvedRecords = creditRecordRepository.findByStatus(CreditStatus.APPROVED);
        Map<Long, BigDecimal> studentCreditMap = approvedRecords.stream()
                .collect(Collectors.groupingBy(
                        CreditRecord::getStudentId,
                        Collectors.reducing(BigDecimal.ZERO, CreditRecord::getCredit, BigDecimal::add)
                ));

        Map<Long, Student> studentMap = studentService.list().stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        List<Map.Entry<Long, BigDecimal>> rankingEntries = studentCreditMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topN)
                .toList();

        List<StudentCreditRankingResponse> ranking = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, BigDecimal> entry : rankingEntries) {
            Student student = studentMap.get(entry.getKey());
            if (student == null) {
                continue;
            }
            ranking.add(StudentCreditRankingResponse.builder()
                    .rank(rank++)
                    .studentId(student.getId())
                    .studentNo(student.getStudentNo())
                    .studentName(student.getName())
                    .totalCredit(entry.getValue())
                    .build());
        }
        return ranking;
    }

    private BigDecimal resolveCredit(CreditRule creditRule, Activity activity) {
        BigDecimal ruleCredit = creditRule.getBaseCredit();
        BigDecimal activityMaxCredit = activity.getMaxCredit();
        if (ruleCredit == null || activityMaxCredit == null) {
            throw new BusinessException("学分规则或活动学分配置异常");
        }
        return ruleCredit.min(activityMaxCredit);
    }

    private CreditRecord updateRecordStatus(Long recordId,
                                            CreditStatus targetStatus,
                                            String reviewRemark,
                                            String operatorRole) {
        try {
            CreditRecord creditRecord = creditRecordRepository.findById(recordId)
                    .orElseThrow(() -> new BusinessException("学分记录不存在，id=" + recordId));
            if (creditRecord.getStatus() != CreditStatus.PENDING) {
                throw new BusinessException("仅待审核记录可变更状态，当前状态=" + creditRecord.getStatus());
            }
            creditRecord.setStatus(targetStatus);
            if (reviewRemark != null && !reviewRemark.isBlank()) {
                creditRecord.setRemark(reviewRemark);
            }
            CreditRecord saved = creditRecordRepository.save(creditRecord);
            logReviewResult(recordId, targetStatus, operatorRole, reviewRemark, true, null);
            return saved;
        } catch (BusinessException ex) {
            logReviewResult(recordId, targetStatus, operatorRole, reviewRemark, false, ex.getMessage());
            throw ex;
        }
    }

    private List<DimensionCreditStatResponse> getDimensionStatistics(Function<Student, String> dimensionResolver) {
        List<CreditRecord> approvedRecords = creditRecordRepository.findByStatus(CreditStatus.APPROVED);
        Map<Long, Student> studentMap = studentService.list().stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        Map<String, BigDecimal> dimensionCreditMap = approvedRecords.stream()
                .filter(record -> studentMap.containsKey(record.getStudentId()))
                .collect(Collectors.groupingBy(
                        record -> normalizeDimension(dimensionResolver.apply(studentMap.get(record.getStudentId()))),
                        Collectors.reducing(BigDecimal.ZERO, CreditRecord::getCredit, BigDecimal::add)
                ));

        return dimensionCreditMap.entrySet().stream()
                .map(entry -> DimensionCreditStatResponse.builder()
                        .dimension(entry.getKey())
                        .totalCredit(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(DimensionCreditStatResponse::getDimension))
                .toList();
    }

    private String normalizeDimension(String value) {
        if (value == null || value.isBlank()) {
            return "未设置";
        }
        return value;
    }

    private void validatePageParams(int page, int size) {
        if (page < 0) {
            throw new BusinessException("page 不能小于 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("size 必须在 1 到 100 之间");
        }
    }

    private PageResult<CreditRecord> buildPageResult(Page<CreditRecord> recordPage) {
        return PageResult.<CreditRecord>builder()
                .page(recordPage.getNumber())
                .size(recordPage.getSize())
                .totalElements(recordPage.getTotalElements())
                .totalPages(recordPage.getTotalPages())
                .content(recordPage.getContent())
                .build();
    }

    private CreditBatchReviewResult batchUpdateStatus(List<Long> recordIds,
                                                      CreditStatus targetStatus,
                                                      String reviewRemark,
                                                      String operatorRole) {
        List<Long> normalizedIds = recordIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        int success = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Long recordId : normalizedIds) {
            CreditRecord creditRecord = creditRecordRepository.findById(recordId).orElse(null);
            if (creditRecord == null) {
                logReviewResult(recordId, targetStatus, operatorRole, reviewRemark, false, "学分记录不存在");
                failedIds.add(recordId);
                continue;
            }
            if (creditRecord.getStatus() != CreditStatus.PENDING) {
                logReviewResult(recordId, targetStatus, operatorRole, reviewRemark, false,
                        "仅待审核记录可变更状态，当前状态=" + creditRecord.getStatus());
                failedIds.add(recordId);
                continue;
            }
            creditRecord.setStatus(targetStatus);
            if (reviewRemark != null && !reviewRemark.isBlank()) {
                creditRecord.setRemark(reviewRemark);
            }
            creditRecordRepository.save(creditRecord);
            logReviewResult(recordId, targetStatus, operatorRole, reviewRemark, true, null);
            success++;
        }

        return CreditBatchReviewResult.builder()
                .total(normalizedIds.size())
                .success(success)
                .failedIds(failedIds)
                .build();
    }

    private void logReviewResult(Long recordId,
                                 CreditStatus targetStatus,
                                 String operatorRole,
                                 String reviewRemark,
                                 boolean success,
                                 String detail) {
        creditReviewLogService.saveLog(
                recordId,
                resolveReviewAction(targetStatus),
                operatorRole,
                reviewRemark,
                success,
                detail
        );
    }

    private CreditReviewAction resolveReviewAction(CreditStatus targetStatus) {
        if (targetStatus == CreditStatus.APPROVED) {
            return CreditReviewAction.APPROVE;
        }
        if (targetStatus == CreditStatus.REJECTED) {
            return CreditReviewAction.REJECT;
        }
        throw new BusinessException("不支持的审核目标状态：" + targetStatus);
    }
}
