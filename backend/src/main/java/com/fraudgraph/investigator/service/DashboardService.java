package com.fraudgraph.investigator.service;

import com.fraudgraph.investigator.dto.DashboardSummaryDto;
import com.fraudgraph.investigator.repository.GraphQueryRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final GraphQueryRepository graphQueryRepository;

    public DashboardService(GraphQueryRepository graphQueryRepository) {
        this.graphQueryRepository = graphQueryRepository;
    }

    public DashboardSummaryDto summary() {
        return new DashboardSummaryDto(
                graphQueryRepository.countOpenAlerts(),
                graphQueryRepository.countActiveCases(),
                graphQueryRepository.countHighRiskEntities(),
                graphQueryRepository.suspiciousAmount(),
                graphQueryRepository.patternSummaries()
        );
    }
}
