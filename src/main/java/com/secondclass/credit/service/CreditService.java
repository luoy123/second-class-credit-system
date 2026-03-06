package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.CreditApplyRequest;
import com.secondclass.credit.domain.dto.CreditSummaryResponse;
import com.secondclass.credit.domain.entity.Activity;
import com.secondclass.credit.domain.entity.CreditRecord;
import com.secondclass.credit.domain.entity.CreditRule;
import com.secondclass.credit.domain.entity.Student;
import com.secondclass.credit.domain.enums.CreditStatus;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.CreditRecordRepository;
import com.secondclass.credit.repository.CreditRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRecordRepository creditRecordRepository;
    private final CreditRuleRepository creditRuleRepository;
    private final StudentService studentService;
    private final ActivityService activityService;

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
        creditRecord.setStatus(CreditStatus.APPROVED);
        creditRecord.setRemark(request.getRemark());
        return creditRecordRepository.save(creditRecord);
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

    private BigDecimal resolveCredit(CreditRule creditRule, Activity activity) {
        BigDecimal ruleCredit = creditRule.getBaseCredit();
        BigDecimal activityMaxCredit = activity.getMaxCredit();
        if (ruleCredit == null || activityMaxCredit == null) {
            throw new BusinessException("学分规则或活动学分配置异常");
        }
        return ruleCredit.min(activityMaxCredit);
    }
}
