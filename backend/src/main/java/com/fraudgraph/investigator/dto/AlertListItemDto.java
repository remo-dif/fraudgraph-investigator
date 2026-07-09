package com.fraudgraph.investigator.dto;

import java.time.OffsetDateTime;

public record AlertListItemDto(
        String id,
        String title,
        String status,
        String severity,
        int riskScore,
        String entityId,
        String entityLabel,
        OffsetDateTime createdAt
) {
}
