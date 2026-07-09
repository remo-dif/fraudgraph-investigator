package com.fraudgraph.investigator.hypergraph;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HypergraphPatternDetectorTest {
    private final HypergraphPatternDetector detector = new HypergraphPatternDetector();

    @Test
    void detectsSharedDevicePatternAcrossAccounts() {
        HypergraphDTO graph = new HypergraphDTO(
                List.of(
                        node("dev-001", "Device"),
                        node("acct-001", "Account"),
                        node("acct-002", "Account"),
                        node("acct-003", "Account")
                ),
                List.of(
                        edge("event-001", "dev-001", "acct-001"),
                        edge("event-002", "dev-001", "acct-002"),
                        edge("event-003", "dev-001", "acct-003")
                ),
                List.of()
        );

        assertThat(detector.detect(graph))
                .anyMatch(pattern -> pattern.patternType().equals("SHARED_DEVICE_PATTERN"));
    }

    private HyperNodeDTO node(String id, String type) {
        return new HyperNodeDTO(id, type, id, 50, Map.of());
    }

    private HyperEdgeDTO edge(String id, String... nodeIds) {
        return new HyperEdgeDTO(id, "PaymentEvent", id, 80, List.of(nodeIds), OffsetDateTime.now(), Map.of());
    }
}
