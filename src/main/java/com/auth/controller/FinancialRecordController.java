package com.auth.controller;

import com.auth.dto.ApiResponse;
import com.auth.dto.CreateFinancialRecordRequest;
import com.auth.dto.FinancialRecordResponse;
import com.auth.entities.RecordType;
import com.auth.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService service;

    // Admin + Analyst only
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<FinancialRecordResponse> create(
            @Valid @RequestBody CreateFinancialRecordRequest request,
            Authentication auth) {
        UUID userId = UUID.fromString((String) auth.getPrincipal());
        return ResponseEntity.ok(service.createRecord(request, userId));
    }
    //Get all Records
    @GetMapping
    public ResponseEntity<List<FinancialRecordResponse>> getAll() {
        return ResponseEntity.ok(service.getAllRecords());
    }
    // Filter Records by type, category, starting and ending date
    @GetMapping("/filter")
    public ResponseEntity<List<FinancialRecordResponse>> filter(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.filterRecords(type, category, from, to));
    }

    // Admin + Analyst only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<?> delete(@PathVariable UUID id, Authentication auth) {
        UUID userId = UUID.fromString((String) auth.getPrincipal());
        service.deleteRecord(id, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Record deleted successfully"));
    }
}