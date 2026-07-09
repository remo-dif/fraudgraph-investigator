package com.fraudgraph.investigator.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CaseDto(
        String id,
        String title,
        String status,
        int riskScore,
        OffsetDateTime createdAt,
        List<String> alertIds,
        List<String> recommendedActions
) {
}
