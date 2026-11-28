package com.fraudgraph.investigator.hypergraph;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.context.annotation.Profile("demo")
@RestController
@RequestMapping("/api/hypergraph")
public class HypergraphController {
    private final HypergraphService hypergraphService;

    public HypergraphController(HypergraphService hypergraphService) {
        this.hypergraphService = hypergraphService;
    }

    @GetMapping("/cases/{caseId}")
    public HypergraphDTO caseHypergraph(@PathVariable String caseId) {
        return hypergraphService.caseHypergraph(caseId);
    }

    @GetMapping("/entities/{entityId}/neighborhood")
    public HypergraphDTO entityNeighborhood(@PathVariable String entityId) {
        return hypergraphService.entityNeighborhood(entityId);
    }

    @GetMapping("/events/{eventId}")
    public HypergraphDTO eventHypergraph(@PathVariable String eventId) {
        return hypergraphService.eventHypergraph(eventId);
    }

    @GetMapping("/patterns/suspicious-clusters")
    public List<SuspiciousHypergraphPatternDTO> suspiciousClusters() {
        return hypergraphService.suspiciousClusters();
    }
}
