package com.fraudgraph.investigator.hypergraph;

import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class HypergraphRiskScorer {
    public int scoreEdge(Collection<HyperNodeDTO> nodes, int baseRisk) {
        int participantRisk = nodes.stream()
                .map(HyperNodeDTO::riskScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        int densityBonus = Math.min(15, Math.max(0, nodes.size() - 3) * 3);
        return Math.min(100, Math.max(baseRisk, participantRisk) + densityBonus);
    }

    public String severity(int score) {
        if (score >= 90) {
            return "CRITICAL";
        }
        if (score >= 75) {
            return "HIGH";
        }
        if (score >= 55) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
