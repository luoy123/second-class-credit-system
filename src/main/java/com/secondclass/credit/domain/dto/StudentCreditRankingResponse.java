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
public class StudentCreditRankingResponse {

    private Integer rank;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private BigDecimal totalCredit;
}
