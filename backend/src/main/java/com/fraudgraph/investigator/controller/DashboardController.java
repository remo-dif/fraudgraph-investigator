package com.fraudgraph.investigator.controller;

import com.fraudgraph.investigator.dto.DashboardSummaryDto;
import com.fraudgraph.investigator.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.context.annotation.Profile("demo")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryDto summary() {
        return dashboardService.summary();
    }
}
