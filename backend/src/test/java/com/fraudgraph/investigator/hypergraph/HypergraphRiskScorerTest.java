package com.fraudgraph.investigator.hypergraph;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HypergraphRiskScorerTest {
    private final HypergraphRiskScorer scorer = new HypergraphRiskScorer();

    @Test
    void scoresEdgeUsingHighestParticipantRiskAndDensityBonus() {
        int score = scorer.scoreEdge(List.of(
                new HyperNodeDTO("acct-001", "Account", "Account", 72, Map.of()),
                new HyperNodeDTO("dev-001", "Device", "Device", 91, Map.of()),
                new HyperNodeDTO("ip-001", "IP Address", "IP", 40, Map.of()),
                new HyperNodeDTO("merch-001", "Merchant", "Merchant", 55, Map.of())
        ), 60);

        assertThat(score).isEqualTo(94);
        assertThat(scorer.severity(score)).isEqualTo("CRITICAL");
    }
}
