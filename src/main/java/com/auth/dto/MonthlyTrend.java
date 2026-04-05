package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyTrend {

    private String month;
    private Double totalIncome;
    private Double totalExpense;
}