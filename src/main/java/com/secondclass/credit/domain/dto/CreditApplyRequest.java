package com.secondclass.credit.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreditApplyRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotBlank(message = "学分分类不能为空")
    @Size(max = 64, message = "学分分类长度不能超过64")
    private String category;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}
