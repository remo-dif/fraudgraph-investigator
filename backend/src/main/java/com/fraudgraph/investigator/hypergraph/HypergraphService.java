package com.fraudgraph.investigator.hypergraph;

import java.util.List;
import org.springframework.stereotype.Service;

@org.springframework.context.annotation.Profile("demo")
@Service
public class HypergraphService {
    private final HypergraphRepository repository;
    private final HypergraphPatternDetector patternDetector;

    public HypergraphService(HypergraphRepository repository, HypergraphPatternDetector patternDetector) {
        this.repository = repository;
        this.patternDetector = patternDetector;
    }

    public HypergraphDTO caseHypergraph(String caseId) {
        return withPatterns(repository.findCaseHypergraph(caseId));
    }

    public HypergraphDTO entityNeighborhood(String entityId) {
        return withPatterns(repository.findEntityNeighborhood(entityId));
    }

    public HypergraphDTO eventHypergraph(String eventId) {
        return withPatterns(repository.findEvent(eventId));
    }

    public List<SuspiciousHypergraphPatternDTO> suspiciousClusters() {
        return patternDetector.detect(repository.findSuspiciousClusterCandidateGraph());
    }

    private HypergraphDTO withPatterns(HypergraphDTO hypergraph) {
        return new HypergraphDTO(hypergraph.nodes(), hypergraph.hyperedges(), patternDetector.detect(hypergraph));
    }
}
