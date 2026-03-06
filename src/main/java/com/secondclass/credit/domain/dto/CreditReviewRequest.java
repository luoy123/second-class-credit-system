package com.secondclass.credit.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreditReviewRequest {

    @Size(max = 255, message = "审核备注长度不能超过255")
    private String remark;
}
