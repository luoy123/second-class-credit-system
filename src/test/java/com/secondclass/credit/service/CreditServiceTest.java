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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private CreditRecordRepository creditRecordRepository;

    @Mock
    private CreditRuleRepository creditRuleRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private CreditService creditService;

    @Test
    void applyShouldUseMinCreditAndSaveApprovedRecord() {
        CreditApplyRequest request = new CreditApplyRequest();
        request.setStudentId(1L);
        request.setActivityId(2L);
        request.setCategory("志愿服务");
        request.setRemark("test");

        Student student = new Student();
        student.setId(1L);
        student.setStudentNo("20260001");
        student.setName("张三");

        Activity activity = new Activity();
        activity.setId(2L);
        activity.setMaxCredit(new BigDecimal("1.50"));

        CreditRule creditRule = new CreditRule();
        creditRule.setCategory("志愿服务");
        creditRule.setBaseCredit(new BigDecimal("2.00"));
        creditRule.setEnabled(true);

        when(studentService.findById(1L)).thenReturn(student);
        when(activityService.findById(2L)).thenReturn(activity);
        when(creditRuleRepository.findFirstByCategoryAndEnabledTrue("志愿服务")).thenReturn(Optional.of(creditRule));
        when(creditRecordRepository.save(any(CreditRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditRecord saved = creditService.apply(request);

        assertEquals(1L, saved.getStudentId());
        assertEquals(2L, saved.getActivityId());
        assertEquals(CreditStatus.APPROVED, saved.getStatus());
        assertEquals(0, new BigDecimal("1.50").compareTo(saved.getCredit()));
        verify(creditRecordRepository).save(any(CreditRecord.class));
    }

    @Test
    void applyShouldThrowWhenRuleMissing() {
        CreditApplyRequest request = new CreditApplyRequest();
        request.setStudentId(1L);
        request.setActivityId(2L);
        request.setCategory("不存在的分类");

        when(studentService.findById(1L)).thenReturn(new Student());
        when(activityService.findById(2L)).thenReturn(new Activity());
        when(creditRuleRepository.findFirstByCategoryAndEnabledTrue("不存在的分类")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> creditService.apply(request));
        verify(creditRecordRepository, never()).save(any(CreditRecord.class));
    }

    @Test
    void getStudentSummaryShouldAggregateByCategory() {
        Student student = new Student();
        student.setId(1L);
        student.setStudentNo("20260001");
        student.setName("张三");

        CreditRecord r1 = new CreditRecord();
        r1.setCategory("A");
        r1.setCredit(new BigDecimal("1.00"));
        r1.setStatus(CreditStatus.APPROVED);

        CreditRecord r2 = new CreditRecord();
        r2.setCategory("B");
        r2.setCredit(new BigDecimal("2.00"));
        r2.setStatus(CreditStatus.APPROVED);

        CreditRecord r3 = new CreditRecord();
        r3.setCategory("A");
        r3.setCredit(new BigDecimal("0.50"));
        r3.setStatus(CreditStatus.APPROVED);

        when(studentService.findById(1L)).thenReturn(student);
        when(creditRecordRepository.findByStudentIdAndStatus(1L, CreditStatus.APPROVED))
                .thenReturn(List.of(r1, r2, r3));

        CreditSummaryResponse summary = creditService.getStudentSummary(1L);

        assertEquals("20260001", summary.getStudentNo());
        assertEquals("张三", summary.getStudentName());
        assertEquals(0, new BigDecimal("3.50").compareTo(summary.getTotalCredit()));
        assertEquals(2, summary.getCategoryCredits().size());
        assertEquals("A", summary.getCategoryCredits().get(0).getCategory());
        assertEquals(0, new BigDecimal("1.50").compareTo(summary.getCategoryCredits().get(0).getCredit()));
        assertEquals("B", summary.getCategoryCredits().get(1).getCategory());
        assertEquals(0, new BigDecimal("2.00").compareTo(summary.getCategoryCredits().get(1).getCredit()));
    }
}
