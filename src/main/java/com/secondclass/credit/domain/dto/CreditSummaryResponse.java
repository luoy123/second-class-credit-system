package com.secondclass.credit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditSummaryResponse {

    private Long studentId;
    private String studentNo;
    private String studentName;
    private BigDecimal totalCredit;
    private List<CategoryCreditItem> categoryCredits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCreditItem {
        private String category;
        private BigDecimal credit;
    }
}
