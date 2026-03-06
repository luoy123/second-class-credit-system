package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.CreditApplyRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Mock
    private CreditReviewLogService creditReviewLogService;

    @InjectMocks
    private CreditService creditService;

    @Test
    void applyShouldUseMinCreditAndSavePendingRecord() {
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
        assertEquals(CreditStatus.PENDING, saved.getStatus());
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
    void approveShouldUpdatePendingRecordToApproved() {
        CreditRecord record = new CreditRecord();
        record.setId(10L);
        record.setStatus(CreditStatus.PENDING);
        record.setRemark("old");

        when(creditRecordRepository.findById(10L)).thenReturn(Optional.of(record));
        when(creditRecordRepository.save(any(CreditRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditRecord result = creditService.approve(10L, "通过", "ADMIN");

        assertEquals(CreditStatus.APPROVED, result.getStatus());
        assertEquals("通过", result.getRemark());
        verify(creditReviewLogService).saveLog(
                eq(10L),
                eq(CreditReviewAction.APPROVE),
                eq("ADMIN"),
                eq("通过"),
                eq(true),
                isNull()
        );
    }

    @Test
    void rejectShouldUpdatePendingRecordToRejected() {
        CreditRecord record = new CreditRecord();
        record.setId(11L);
        record.setStatus(CreditStatus.PENDING);

        when(creditRecordRepository.findById(11L)).thenReturn(Optional.of(record));
        when(creditRecordRepository.save(any(CreditRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditRecord result = creditService.reject(11L, "材料不足", "ADMIN");

        assertEquals(CreditStatus.REJECTED, result.getStatus());
        assertEquals("材料不足", result.getRemark());
        verify(creditReviewLogService).saveLog(
                eq(11L),
                eq(CreditReviewAction.REJECT),
                eq("ADMIN"),
                eq("材料不足"),
                eq(true),
                isNull()
        );
    }

    @Test
    void approveShouldThrowWhenRecordIsNotPending() {
        CreditRecord record = new CreditRecord();
        record.setId(12L);
        record.setStatus(CreditStatus.APPROVED);

        when(creditRecordRepository.findById(12L)).thenReturn(Optional.of(record));

        assertThrows(BusinessException.class, () -> creditService.approve(12L, "重复审核", "ADMIN"));
        verify(creditRecordRepository, never()).save(any(CreditRecord.class));
        verify(creditReviewLogService).saveLog(
                eq(12L),
                eq(CreditReviewAction.APPROVE),
                eq("ADMIN"),
                eq("重复审核"),
                eq(false),
                eq("仅待审核记录可变更状态，当前状态=APPROVED")
        );
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

    @Test
    void getCategoryStatisticsShouldAggregateAndSort() {
        CreditRecord r1 = new CreditRecord();
        r1.setCategory("B");
        r1.setCredit(new BigDecimal("1.00"));
        r1.setStatus(CreditStatus.APPROVED);

        CreditRecord r2 = new CreditRecord();
        r2.setCategory("A");
        r2.setCredit(new BigDecimal("2.00"));
        r2.setStatus(CreditStatus.APPROVED);

        CreditRecord r3 = new CreditRecord();
        r3.setCategory("A");
        r3.setCredit(new BigDecimal("0.50"));
        r3.setStatus(CreditStatus.APPROVED);

        when(creditRecordRepository.findByStatus(CreditStatus.APPROVED)).thenReturn(List.of(r1, r2, r3));

        var result = creditService.getCategoryStatistics();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getCategory());
        assertEquals(0, new BigDecimal("2.50").compareTo(result.get(0).getTotalCredit()));
        assertEquals("B", result.get(1).getCategory());
        assertEquals(0, new BigDecimal("1.00").compareTo(result.get(1).getTotalCredit()));
    }

    @Test
    void getMajorStatisticsShouldAggregateByStudentMajor() {
        Student s1 = new Student();
        s1.setId(1L);
        s1.setMajor("软件工程");

        Student s2 = new Student();
        s2.setId(2L);
        s2.setMajor(null);

        CreditRecord r1 = new CreditRecord();
        r1.setStudentId(1L);
        r1.setCredit(new BigDecimal("1.00"));

        CreditRecord r2 = new CreditRecord();
        r2.setStudentId(1L);
        r2.setCredit(new BigDecimal("0.50"));

        CreditRecord r3 = new CreditRecord();
        r3.setStudentId(2L);
        r3.setCredit(new BigDecimal("2.00"));

        when(creditRecordRepository.findByStatus(CreditStatus.APPROVED)).thenReturn(List.of(r1, r2, r3));
        when(studentService.list()).thenReturn(List.of(s1, s2));

        var result = creditService.getMajorStatistics();

        assertEquals(2, result.size());
        assertEquals("未设置", result.get(0).getDimension());
        assertEquals(0, new BigDecimal("2.00").compareTo(result.get(0).getTotalCredit()));
        assertEquals("软件工程", result.get(1).getDimension());
        assertEquals(0, new BigDecimal("1.50").compareTo(result.get(1).getTotalCredit()));
    }

    @Test
    void getGradeStatisticsShouldAggregateByStudentGrade() {
        Student s1 = new Student();
        s1.setId(1L);
        s1.setGrade("2025");

        Student s2 = new Student();
        s2.setId(2L);
        s2.setGrade("2026");

        CreditRecord r1 = new CreditRecord();
        r1.setStudentId(1L);
        r1.setCredit(new BigDecimal("1.00"));

        CreditRecord r2 = new CreditRecord();
        r2.setStudentId(2L);
        r2.setCredit(new BigDecimal("2.00"));

        CreditRecord r3 = new CreditRecord();
        r3.setStudentId(2L);
        r3.setCredit(new BigDecimal("0.50"));

        when(creditRecordRepository.findByStatus(CreditStatus.APPROVED)).thenReturn(List.of(r1, r2, r3));
        when(studentService.list()).thenReturn(List.of(s1, s2));

        var result = creditService.getGradeStatistics();

        assertEquals(2, result.size());
        assertEquals("2025", result.get(0).getDimension());
        assertEquals(0, new BigDecimal("1.00").compareTo(result.get(0).getTotalCredit()));
        assertEquals("2026", result.get(1).getDimension());
        assertEquals(0, new BigDecimal("2.50").compareTo(result.get(1).getTotalCredit()));
    }

    @Test
    void getMonthlyStatisticsShouldReturnTwelveMonthsAndAggregateCurrentYear() {
        CreditRecord january = new CreditRecord();
        january.setCredit(new BigDecimal("1.00"));
        january.setCreatedAt(LocalDateTime.of(2026, 1, 10, 10, 0));

        CreditRecord march = new CreditRecord();
        march.setCredit(new BigDecimal("2.00"));
        march.setCreatedAt(LocalDateTime.of(2026, 3, 8, 9, 0));

        CreditRecord lastYear = new CreditRecord();
        lastYear.setCredit(new BigDecimal("9.00"));
        lastYear.setCreatedAt(LocalDateTime.of(2025, 3, 8, 9, 0));

        when(creditRecordRepository.findByStatus(CreditStatus.APPROVED)).thenReturn(List.of(january, march, lastYear));

        var result = creditService.getMonthlyStatistics(2026);

        assertEquals(12, result.size());
        assertEquals(0, new BigDecimal("1.00").compareTo(result.get(0).getTotalCredit()));
        assertEquals(0, new BigDecimal("2.00").compareTo(result.get(2).getTotalCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.get(1).getTotalCredit()));
    }

    @Test
    void getStudentRankingShouldReturnTopN() {
        Student s1 = new Student();
        s1.setId(1L);
        s1.setStudentNo("20260001");
        s1.setName("张三");

        Student s2 = new Student();
        s2.setId(2L);
        s2.setStudentNo("20260002");
        s2.setName("李四");

        CreditRecord r1 = new CreditRecord();
        r1.setStudentId(1L);
        r1.setCredit(new BigDecimal("1.00"));

        CreditRecord r2 = new CreditRecord();
        r2.setStudentId(1L);
        r2.setCredit(new BigDecimal("2.00"));

        CreditRecord r3 = new CreditRecord();
        r3.setStudentId(2L);
        r3.setCredit(new BigDecimal("2.50"));

        when(creditRecordRepository.findByStatus(CreditStatus.APPROVED)).thenReturn(List.of(r1, r2, r3));
        when(studentService.list()).thenReturn(List.of(s1, s2));

        var result = creditService.getStudentRanking(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getRank());
        assertEquals(1L, result.get(0).getStudentId());
        assertEquals("20260001", result.get(0).getStudentNo());
        assertEquals(0, new BigDecimal("3.00").compareTo(result.get(0).getTotalCredit()));
    }

    @Test
    void getStudentRankingShouldThrowWhenTopNIsInvalid() {
        assertThrows(BusinessException.class, () -> creditService.getStudentRanking(0));
    }

    @Test
    void listStudentRecordsPageShouldFilterByStatus() {
        Student student = new Student();
        student.setId(1L);

        CreditRecord record = new CreditRecord();
        record.setId(100L);
        record.setStudentId(1L);
        record.setStatus(CreditStatus.PENDING);

        when(studentService.findById(1L)).thenReturn(student);
        when(creditRecordRepository.findByStudentIdAndStatus(1L, CreditStatus.PENDING, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(record), PageRequest.of(0, 10), 1));

        var result = creditService.listStudentRecordsPage(1L, CreditStatus.PENDING, 0, 10);

        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(100L, result.getContent().get(0).getId());
    }

    @Test
    void listStudentRecordsPageShouldThrowWhenSizeOutOfRange() {
        when(studentService.findById(1L)).thenReturn(new Student());
        assertThrows(BusinessException.class, () -> creditService.listStudentRecordsPage(1L, null, 0, 101));
    }

    @Test
    void listPendingRecordsPageShouldReturnPendingOnly() {
        CreditRecord pendingRecord = new CreditRecord();
        pendingRecord.setId(200L);
        pendingRecord.setStatus(CreditStatus.PENDING);

        when(creditRecordRepository.findByStatus(CreditStatus.PENDING, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(pendingRecord), PageRequest.of(0, 5), 1));

        var result = creditService.listPendingRecordsPage(0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(CreditStatus.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    void batchApproveShouldReturnFailedIdsWhenSomeRecordsAreInvalid() {
        CreditRecord pendingRecord = new CreditRecord();
        pendingRecord.setId(1L);
        pendingRecord.setStatus(CreditStatus.PENDING);

        CreditRecord approvedRecord = new CreditRecord();
        approvedRecord.setId(2L);
        approvedRecord.setStatus(CreditStatus.APPROVED);

        when(creditRecordRepository.findById(1L)).thenReturn(Optional.of(pendingRecord));
        when(creditRecordRepository.findById(2L)).thenReturn(Optional.of(approvedRecord));
        when(creditRecordRepository.findById(3L)).thenReturn(Optional.empty());
        when(creditRecordRepository.save(any(CreditRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = creditService.batchApprove(Arrays.asList(1L, 2L, 3L, 1L, null), "批量通过", "ADMIN");

        assertEquals(3, result.getTotal());
        assertEquals(1, result.getSuccess());
        assertEquals(2, result.getFailedIds().size());
        assertEquals(CreditStatus.APPROVED, pendingRecord.getStatus());
        assertEquals("批量通过", pendingRecord.getRemark());
        verify(creditReviewLogService).saveLog(
                eq(1L),
                eq(CreditReviewAction.APPROVE),
                eq("ADMIN"),
                eq("批量通过"),
                eq(true),
                isNull()
        );
        verify(creditReviewLogService).saveLog(
                eq(2L),
                eq(CreditReviewAction.APPROVE),
                eq("ADMIN"),
                eq("批量通过"),
                eq(false),
                eq("仅待审核记录可变更状态，当前状态=APPROVED")
        );
        verify(creditReviewLogService).saveLog(
                eq(3L),
                eq(CreditReviewAction.APPROVE),
                eq("ADMIN"),
                eq("批量通过"),
                eq(false),
                eq("学分记录不存在")
        );
    }

    @Test
    void batchRejectShouldUpdateAllPendingRecords() {
        CreditRecord pendingRecord1 = new CreditRecord();
        pendingRecord1.setId(11L);
        pendingRecord1.setStatus(CreditStatus.PENDING);

        CreditRecord pendingRecord2 = new CreditRecord();
        pendingRecord2.setId(12L);
        pendingRecord2.setStatus(CreditStatus.PENDING);

        when(creditRecordRepository.findById(11L)).thenReturn(Optional.of(pendingRecord1));
        when(creditRecordRepository.findById(12L)).thenReturn(Optional.of(pendingRecord2));
        when(creditRecordRepository.save(any(CreditRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = creditService.batchReject(List.of(11L, 12L), "批量驳回", "ADMIN");

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getSuccess());
        assertEquals(0, result.getFailedIds().size());
        assertEquals(CreditStatus.REJECTED, pendingRecord1.getStatus());
        assertEquals(CreditStatus.REJECTED, pendingRecord2.getStatus());
        verify(creditReviewLogService).saveLog(
                eq(11L),
                eq(CreditReviewAction.REJECT),
                eq("ADMIN"),
                eq("批量驳回"),
                eq(true),
                isNull()
        );
        verify(creditReviewLogService).saveLog(
                eq(12L),
                eq(CreditReviewAction.REJECT),
                eq("ADMIN"),
                eq("批量驳回"),
                eq(true),
                isNull()
        );
    }
}
