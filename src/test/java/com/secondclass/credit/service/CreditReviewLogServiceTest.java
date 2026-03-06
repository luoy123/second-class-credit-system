package com.secondclass.credit.service;

import com.secondclass.credit.domain.entity.CreditReviewLog;
import com.secondclass.credit.domain.enums.CreditReviewAction;
import com.secondclass.credit.repository.CreditReviewLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

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
}
