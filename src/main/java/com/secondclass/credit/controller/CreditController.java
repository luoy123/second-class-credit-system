package com.secondclass.credit.controller;

import com.secondclass.credit.common.ApiResponse;
import com.secondclass.credit.domain.dto.CategoryCreditStatResponse;
import com.secondclass.credit.domain.dto.CreditApplyRequest;
import com.secondclass.credit.domain.dto.CreditReviewRequest;
import com.secondclass.credit.domain.dto.CreditSummaryResponse;
import com.secondclass.credit.domain.dto.DimensionCreditStatResponse;
import com.secondclass.credit.domain.dto.MonthlyCreditStatResponse;
import com.secondclass.credit.domain.dto.StudentCreditRankingResponse;
import com.secondclass.credit.domain.entity.CreditRecord;
import com.secondclass.credit.service.CreditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;

    @PostMapping("/apply")
    public ApiResponse<CreditRecord> apply(@Valid @RequestBody CreditApplyRequest request) {
        return ApiResponse.success(creditService.apply(request));
    }

    @PostMapping("/{recordId}/approve")
    public ApiResponse<CreditRecord> approve(
            @PathVariable Long recordId,
            @Valid @RequestBody(required = false) CreditReviewRequest request) {
        String remark = request == null ? null : request.getRemark();
        return ApiResponse.success(creditService.approve(recordId, remark));
    }

    @PostMapping("/{recordId}/reject")
    public ApiResponse<CreditRecord> reject(
            @PathVariable Long recordId,
            @Valid @RequestBody(required = false) CreditReviewRequest request) {
        String remark = request == null ? null : request.getRemark();
        return ApiResponse.success(creditService.reject(recordId, remark));
    }

    @GetMapping("/students/{studentId}/summary")
    public ApiResponse<CreditSummaryResponse> getStudentSummary(@PathVariable Long studentId) {
        return ApiResponse.success(creditService.getStudentSummary(studentId));
    }

    @GetMapping("/students/{studentId}/records")
    public ApiResponse<List<CreditRecord>> listStudentRecords(@PathVariable Long studentId) {
        return ApiResponse.success(creditService.listStudentRecords(studentId));
    }

    @GetMapping("/analytics/categories")
    public ApiResponse<List<CategoryCreditStatResponse>> getCategoryStatistics() {
        return ApiResponse.success(creditService.getCategoryStatistics());
    }

    @GetMapping("/analytics/majors")
    public ApiResponse<List<DimensionCreditStatResponse>> getMajorStatistics() {
        return ApiResponse.success(creditService.getMajorStatistics());
    }

    @GetMapping("/analytics/grades")
    public ApiResponse<List<DimensionCreditStatResponse>> getGradeStatistics() {
        return ApiResponse.success(creditService.getGradeStatistics());
    }

    @GetMapping("/analytics/monthly")
    public ApiResponse<List<MonthlyCreditStatResponse>> getMonthlyStatistics(
            @RequestParam int year) {
        return ApiResponse.success(creditService.getMonthlyStatistics(year));
    }

    @GetMapping("/analytics/ranking")
    public ApiResponse<List<StudentCreditRankingResponse>> getStudentRanking(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "topN 不能小于 1") @Max(value = 100, message = "topN 不能大于 100") int topN) {
        return ApiResponse.success(creditService.getStudentRanking(topN));
    }
}
