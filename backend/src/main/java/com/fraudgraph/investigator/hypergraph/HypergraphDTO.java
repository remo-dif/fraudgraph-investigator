package com.fraudgraph.investigator.hypergraph;

import java.util.List;

public record HypergraphDTO(
        List<HyperNodeDTO> nodes,
        List<HyperEdgeDTO> hyperedges,
        List<SuspiciousHypergraphPatternDTO> suspiciousPatterns
) {
}
