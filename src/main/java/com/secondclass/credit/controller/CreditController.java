package com.secondclass.credit.controller;

import com.secondclass.credit.common.ApiResponse;
import com.secondclass.credit.domain.dto.CreditApplyRequest;
import com.secondclass.credit.domain.dto.CreditSummaryResponse;
import com.secondclass.credit.domain.entity.CreditRecord;
import com.secondclass.credit.service.CreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;

    @PostMapping("/apply")
    public ApiResponse<CreditRecord> apply(@Valid @RequestBody CreditApplyRequest request) {
        return ApiResponse.success(creditService.apply(request));
    }

    @GetMapping("/students/{studentId}/summary")
    public ApiResponse<CreditSummaryResponse> getStudentSummary(@PathVariable Long studentId) {
        return ApiResponse.success(creditService.getStudentSummary(studentId));
    }

    @GetMapping("/students/{studentId}/records")
    public ApiResponse<List<CreditRecord>> listStudentRecords(@PathVariable Long studentId) {
        return ApiResponse.success(creditService.listStudentRecords(studentId));
    }
}
