package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.CategoryCreditStatResponse;
import com.secondclass.credit.domain.dto.StudentCreditRankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditReportService {

    private final CreditService creditService;

    public String exportCategoryStatisticsCsv() {
        List<CategoryCreditStatResponse> statistics = creditService.getCategoryStatistics();
        StringBuilder builder = new StringBuilder("category,totalCredit\n");
        for (CategoryCreditStatResponse item : statistics) {
            builder.append(toCsvCell(item.getCategory()))
                    .append(',')
                    .append(item.getTotalCredit() == null ? "" : item.getTotalCredit().toPlainString())
                    .append('\n');
        }
        return builder.toString();
    }

    public String exportStudentRankingCsv(int topN) {
        List<StudentCreditRankingResponse> ranking = creditService.getStudentRanking(topN);
        StringBuilder builder = new StringBuilder("rank,studentId,studentNo,studentName,totalCredit\n");
        for (StudentCreditRankingResponse item : ranking) {
            builder.append(item.getRank() == null ? "" : item.getRank()).append(',')
                    .append(item.getStudentId() == null ? "" : item.getStudentId()).append(',')
                    .append(toCsvCell(item.getStudentNo())).append(',')
                    .append(toCsvCell(item.getStudentName())).append(',')
                    .append(item.getTotalCredit() == null ? "" : item.getTotalCredit().toPlainString())
                    .append('\n');
        }
        return builder.toString();
    }

    private String toCsvCell(String value) {
        if (value == null) {
            return "";
        }
        if (!value.contains(",") && !value.contains("\"") && !value.contains("\n")) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
