package com.secondclass.credit.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreditBatchReviewRequest {

    @NotEmpty(message = "记录ID列表不能为空")
    private List<Long> recordIds;

    @Size(max = 255, message = "审核备注长度不能超过255")
    private String remark;
}
