package com.fraudgraph.investigator.hypergraph;

import java.util.List;

public record SuspiciousHypergraphPatternDTO(
        String patternType,
        String severity,
        List<String> involvedNodeIds,
        List<String> involvedHyperedgeIds,
        String explanation,
        String recommendedAction
) {
}
