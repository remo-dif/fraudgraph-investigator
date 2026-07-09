package com.fraudgraph.investigator.service;

import com.fraudgraph.investigator.dto.GraphDto;
import com.fraudgraph.investigator.repository.GraphQueryRepository;
import org.springframework.stereotype.Service;

@Service
public class GraphService {
    private final GraphQueryRepository graphQueryRepository;

    public GraphService(GraphQueryRepository graphQueryRepository) {
        this.graphQueryRepository = graphQueryRepository;
    }

    public GraphDto neighborhood(String entityId, int depth) {
        return graphQueryRepository.graphNeighborhood(entityId, depth);
    }
}
