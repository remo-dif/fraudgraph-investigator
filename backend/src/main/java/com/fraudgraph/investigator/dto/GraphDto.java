package com.fraudgraph.investigator.dto;

import java.util.List;
import java.util.Map;

public record GraphDto(List<GraphNodeDto> nodes, List<GraphEdgeDto> edges) {
    public record GraphNodeDto(String id, String type, String label, Integer riskScore, Map<String, Object> properties) {
    }

    public record GraphEdgeDto(String id, String source, String target, String type, Map<String, Object> properties) {
    }
}
