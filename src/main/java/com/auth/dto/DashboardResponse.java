package com.auth.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {

    private Double totalIncome;
    private Double totalExpense;
    private Double netBalance;
    private List<CategorySummary> categorySummary;
    private List<RecentActivity> recentActivity;
    private String message;
}