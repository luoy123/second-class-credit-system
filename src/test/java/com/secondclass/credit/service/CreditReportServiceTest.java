package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.CategoryCreditStatResponse;
import com.secondclass.credit.domain.dto.StudentCreditRankingResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditReportServiceTest {

    @Mock
    private CreditService creditService;

    @InjectMocks
    private CreditReportService creditReportService;

    @Test
    void exportCategoryStatisticsCsvShouldContainHeaderAndRows() {
        when(creditService.getCategoryStatistics()).thenReturn(List.of(
                CategoryCreditStatResponse.builder().category("志愿服务").totalCredit(new BigDecimal("1.50")).build(),
                CategoryCreditStatResponse.builder().category("学术竞赛").totalCredit(new BigDecimal("2.00")).build()
        ));

        String csv = creditReportService.exportCategoryStatisticsCsv();

        assertTrue(csv.contains("category,totalCredit"));
        assertTrue(csv.contains("志愿服务,1.50"));
        assertTrue(csv.contains("学术竞赛,2.00"));
    }

    @Test
    void exportStudentRankingCsvShouldEscapeSpecialCharacters() {
        when(creditService.getStudentRanking(10)).thenReturn(List.of(
                StudentCreditRankingResponse.builder()
                        .rank(1)
                        .studentId(1L)
                        .studentNo("20260001")
                        .studentName("张,三")
                        .totalCredit(new BigDecimal("3.00"))
                        .build()
        ));

        String csv = creditReportService.exportStudentRankingCsv(10);

        assertTrue(csv.contains("rank,studentId,studentNo,studentName,totalCredit"));
        assertTrue(csv.contains("1,1,20260001,\"张,三\",3.00"));
    }
}
