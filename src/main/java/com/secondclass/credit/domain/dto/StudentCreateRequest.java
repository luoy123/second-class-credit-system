package com.secondclass.credit.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentCreateRequest {

    @NotBlank(message = "学号不能为空")
    @Size(max = 32, message = "学号长度不能超过32")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过64")
    private String name;

    @Size(max = 64, message = "专业长度不能超过64")
    private String major;

    @Size(max = 16, message = "年级长度不能超过16")
    private String grade;
}
