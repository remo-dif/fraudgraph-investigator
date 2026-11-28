package com.fraudgraph.investigator.controller;

import com.fraudgraph.investigator.dto.AlertDetailDto;
import com.fraudgraph.investigator.dto.AlertListItemDto;
import com.fraudgraph.investigator.service.AlertService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.context.annotation.Profile("demo")
@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertListItemDto> listAlerts() {
        return alertService.listAlerts();
    }

    @GetMapping("/{alertId}")
    public AlertDetailDto alertDetail(@PathVariable String alertId) {
        return alertService.getAlert(alertId);
    }
}
