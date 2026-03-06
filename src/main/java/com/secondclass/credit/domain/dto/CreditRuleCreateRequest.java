package com.secondclass.credit.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditRuleCreateRequest {

    @NotBlank(message = "规则分类不能为空")
    @Size(max = 64, message = "规则分类长度不能超过64")
    private String category;

    @NotNull(message = "基础学分不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "基础学分必须大于0")
    private BigDecimal baseCredit;

    @Size(max = 255, message = "规则描述长度不能超过255")
    private String description;

    private Boolean enabled;
}
