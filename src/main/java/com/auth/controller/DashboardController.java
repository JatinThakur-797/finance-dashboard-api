package com.auth.controller;


import com.auth.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    //Get Dashboard have every one access
    @GetMapping
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
    @GetMapping("/trends")
    public ResponseEntity<?> getTrends() {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends());
    }
}