package com.fraudgraph.investigator.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AlertDetailDto(
        String id,
        String title,
        String status,
        String severity,
        int riskScore,
        String description,
        OffsetDateTime createdAt,
        List<String> riskReasons,
        List<TimelineEventDto> timeline,
        List<EntitySummaryDto> relatedEntities,
        GraphDto graph,
        List<String> recommendedActions
) {
}
