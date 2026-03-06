package com.secondclass.credit.controller;

import com.secondclass.credit.common.ApiResponse;
import com.secondclass.credit.domain.dto.CreditRuleCreateRequest;
import com.secondclass.credit.domain.entity.CreditRule;
import com.secondclass.credit.service.CreditRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credit-rules")
public class CreditRuleController {

    private final CreditRuleService creditRuleService;

    @PostMapping
    public ApiResponse<CreditRule> create(@Valid @RequestBody CreditRuleCreateRequest request) {
        return ApiResponse.success(creditRuleService.create(request));
    }

    @GetMapping
    public ApiResponse<List<CreditRule>> list() {
        return ApiResponse.success(creditRuleService.list());
    }
}
