package com.fraudgraph.investigator.hypergraph;

import java.util.Map;

public record HyperNodeDTO(
        String id,
        String type,
        String label,
        Integer riskScore,
        Map<String, Object> metadata
) {
}
