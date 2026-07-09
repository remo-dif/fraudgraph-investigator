package com.fraudgraph.investigator.service;

import com.fraudgraph.investigator.dto.DetectionResultDto;
import com.fraudgraph.investigator.repository.GraphQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DetectionService {
    private final GraphQueryRepository graphQueryRepository;

    public DetectionService(GraphQueryRepository graphQueryRepository) {
        this.graphQueryRepository = graphQueryRepository;
    }

    public List<DetectionResultDto> detect() {
        return graphQueryRepository.detectSuspiciousPatterns();
    }
}
