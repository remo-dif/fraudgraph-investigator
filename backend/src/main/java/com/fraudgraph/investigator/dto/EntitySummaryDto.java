package com.fraudgraph.investigator.dto;

import java.util.Map;

public record EntitySummaryDto(
        String id,
        String type,
        String label,
        Integer riskScore,
        Map<String, Object> properties
) {
}
