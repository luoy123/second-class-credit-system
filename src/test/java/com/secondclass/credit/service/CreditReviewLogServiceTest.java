package com.secondclass.credit.service;

import com.secondclass.credit.domain.entity.CreditReviewLog;
import com.secondclass.credit.domain.enums.CreditReviewAction;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.CreditReviewLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditReviewLogServiceTest {

    @Mock
    private CreditReviewLogRepository creditReviewLogRepository;

    @InjectMocks
    private CreditReviewLogService creditReviewLogService;

    @Test
    void saveLogShouldPersistNormalizedRoleAndFields() {
        creditReviewLogService.saveLog(100L, CreditReviewAction.APPROVE, " admin ", "通过", true, null);

        ArgumentCaptor<CreditReviewLog> captor = ArgumentCaptor.forClass(CreditReviewLog.class);
        verify(creditReviewLogRepository).save(captor.capture());
        CreditReviewLog saved = captor.getValue();

        assertEquals(100L, saved.getRecordId());
        assertEquals(CreditReviewAction.APPROVE, saved.getAction());
        assertEquals("ADMIN", saved.getOperatorRole());
        assertEquals("通过", saved.getRemark());
        assertEquals(true, saved.getSuccess());
        assertEquals(null, saved.getDetail());
    }

    @Test
    void saveLogShouldUseUnknownWhenRoleIsBlank() {
        creditReviewLogService.saveLog(101L, CreditReviewAction.REJECT, " ", "驳回", false, "状态错误");

        ArgumentCaptor<CreditReviewLog> captor = ArgumentCaptor.forClass(CreditReviewLog.class);
        verify(creditReviewLogRepository).save(captor.capture());
        assertEquals("UNKNOWN", captor.getValue().getOperatorRole());
    }

    @Test
    void listLogsPageShouldReturnRepositoryPageResult() {
        CreditReviewLog log = new CreditReviewLog();
        log.setId(1L);
        log.setRecordId(100L);
        log.setAction(CreditReviewAction.APPROVE);

        when(creditReviewLogRepository.search(eq(100L), eq(CreditReviewAction.APPROVE), eq(true), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(java.util.List.of(log), PageRequest.of(0, 10), 1));

        var result = creditReviewLogService.listLogsPage(100L, CreditReviewAction.APPROVE, true, 0, 10);

        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void listLogsPageShouldThrowWhenSizeInvalid() {
        assertThrows(BusinessException.class, () -> creditReviewLogService.listLogsPage(null, null, null, 0, 101));
    }
}
