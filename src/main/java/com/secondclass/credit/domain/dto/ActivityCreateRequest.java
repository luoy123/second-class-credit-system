package com.secondclass.credit.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ActivityCreateRequest {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 128, message = "活动名称长度不能超过128")
    private String title;

    @NotBlank(message = "活动分类不能为空")
    @Size(max = 64, message = "活动分类长度不能超过64")
    private String category;

    @Size(max = 128, message = "组织单位长度不能超过128")
    private String organizer;

    @NotNull(message = "活动日期不能为空")
    private LocalDate activityDate;

    @NotNull(message = "活动最大学分不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "活动最大学分必须大于0")
    private BigDecimal maxCredit;
}
