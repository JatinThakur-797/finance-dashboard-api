package com.auth.dto;

import com.auth.entities.FinancialRecord;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class FinancialRecordResponse {

    private UUID id;
    private Double amount;
    private String type;
    private String category;
    private LocalDate date;
    private String description;
    private String createdByName;  // just the name, not the whole User object
    private String createdByEmail;

    public FinancialRecordResponse(FinancialRecord r) {
        this.id = r.getId();
        this.amount = r.getAmount();
        this.type = r.getType().name();
        this.category = r.getCategory();
        this.date = r.getDate();
        this.description = r.getDescription();
        if (r.getCreatedBy() != null) {
            this.createdByName = r.getCreatedBy().getName();
            this.createdByEmail = r.getCreatedBy().getEmail();
        }
    }
}