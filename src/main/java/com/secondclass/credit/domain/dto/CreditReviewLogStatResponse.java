package com.secondclass.credit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditReviewLogStatResponse {

    private long totalCount;
    private long successCount;
    private long failedCount;
    private long approveCount;
    private long rejectCount;
    private BigDecimal successRate;
}
