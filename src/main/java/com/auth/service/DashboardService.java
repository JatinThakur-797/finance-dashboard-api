package com.auth.service;


import com.auth.dto.CategorySummary;
import com.auth.dto.DashboardResponse;
import com.auth.dto.MonthlyTrend;
import com.auth.dto.RecentActivity;
import com.auth.entities.RecordType;
import com.auth.repository.FinancialRecordRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepo repository;

    public DashboardResponse getDashboard() {
        Double income = repository.getTotalByType(RecordType.INCOME);
        Double expense = repository.getTotalByType(RecordType.EXPENSE);
        double net = income - expense;

        List<CategorySummary> categories = repository.getCategorySummary()
                .stream()
                .map(obj -> new CategorySummary((String) obj[0], (Double) obj[1]))
                .toList();

        List<RecentActivity> recent = repository
                .getRecentRecords(PageRequest.of(0, 5)) // DB-level LIMIT 5
                .stream()
                .map(r -> new RecentActivity(r.getAmount(), r.getCategory(), r.getType().name(), r.getDate()))
                .toList();

        String message = (income == 0 && expense == 0)
                ? "No financial data available yet"
                : "Dashboard loaded successfully";

        return new DashboardResponse(income, expense, net, categories, recent, message);
    }

    public List<MonthlyTrend> getMonthlyTrends() {

        return repository.getMonthlyTrends()
                .stream()
                .map(obj -> new MonthlyTrend(
                        (String) obj[0],
                        obj[1] != null ? (Double) obj[1] : 0.0,
                        obj[2] != null ? (Double) obj[2] : 0.0
                ))
                .toList();
    }
}