package com.fraudgraph.investigator.hypergraph;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record HyperEdgeDTO(
        String id,
        String type,
        String label,
        Integer riskScore,
        List<String> connectedNodeIds,
        OffsetDateTime timestamp,
        Map<String, Object> metadata
) {
}
