package com.fraudgraph.investigator.hypergraph;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Component;

@Component
public class HypergraphMapper {
    private final HypergraphRiskScorer riskScorer;

    public HypergraphMapper(HypergraphRiskScorer riskScorer) {
        this.riskScorer = riskScorer;
    }

    public HyperNodeDTO toNode(Node node) {
        return new HyperNodeDTO(
                node.get("id").asString(),
                displayType(node),
                labelFor(node),
                node.containsKey("riskScore") ? node.get("riskScore").asInt() : null,
                metadata(node)
        );
    }

    public HyperEdgeDTO toEdge(Node event, List<HyperNodeDTO> participants) {
        int riskScore = riskScorer.scoreEdge(participants, event.containsKey("riskScore") ? event.get("riskScore").asInt() : 0);
        return new HyperEdgeDTO(
                event.get("id").asString(),
                displayType(event),
                labelFor(event),
                riskScore,
                participants.stream().map(HyperNodeDTO::id).distinct().toList(),
                event.containsKey("timestamp") ? OffsetDateTime.parse(event.get("timestamp").asString()) : null,
                metadata(event)
        );
    }

    private String displayType(Node node) {
        return node.labels().iterator().next().replace("IPAddress", "IP Address");
    }

    private String labelFor(Node node) {
        Map<String, Object> values = node.asMap();
        Object label = values.getOrDefault("label",
                values.getOrDefault("name",
                        values.getOrDefault("title",
                                values.getOrDefault("accountNumber",
                                        values.getOrDefault("maskedPan",
                                                values.getOrDefault("ip", values.get("id")))))));
        return String.valueOf(label);
    }

    private Map<String, Object> metadata(Node node) {
        Map<String, Object> values = new LinkedHashMap<>(node.asMap());
        values.remove("id");
        values.remove("name");
        values.remove("title");
        values.remove("label");
        return values;
    }
}
