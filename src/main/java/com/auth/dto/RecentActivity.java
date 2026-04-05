package com.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RecentActivity {

    private Double amount;
    private String category;
    private String type;
    private LocalDate date;
}