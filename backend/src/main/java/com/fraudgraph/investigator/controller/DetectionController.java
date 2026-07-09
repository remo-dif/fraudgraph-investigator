package com.fraudgraph.investigator.controller;

import com.fraudgraph.investigator.dto.DetectionResultDto;
import com.fraudgraph.investigator.service.DetectionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/detections")
public class DetectionController {
    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping
    public List<DetectionResultDto> listDetectedPatterns() {
        return detectionService.detect();
    }

    @PostMapping("/run")
    public List<DetectionResultDto> runDetection() {
        return detectionService.detect();
    }
}
