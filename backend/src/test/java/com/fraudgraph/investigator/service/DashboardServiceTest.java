package com.fraudgraph.investigator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fraudgraph.investigator.dto.PatternSummaryDto;
import com.fraudgraph.investigator.repository.GraphQueryRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    private GraphQueryRepository graphQueryRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void composesDashboardSummaryFromGraphRepository() {
        when(graphQueryRepository.countOpenAlerts()).thenReturn(5L);
        when(graphQueryRepository.countActiveCases()).thenReturn(2L);
        when(graphQueryRepository.countHighRiskEntities()).thenReturn(9L);
        when(graphQueryRepository.suspiciousAmount()).thenReturn(BigDecimal.valueOf(12450.25));
        when(graphQueryRepository.patternSummaries()).thenReturn(List.of(new PatternSummaryDto("shared-device", "Shared device", 3, "HIGH")));

        var summary = dashboardService.summary();

        assertThat(summary.openAlerts()).isEqualTo(5);
        assertThat(summary.activeCases()).isEqualTo(2);
        assertThat(summary.highRiskEntities()).isEqualTo(9);
        assertThat(summary.suspiciousAmount()).isEqualByComparingTo("12450.25");
        assertThat(summary.patternSummaries()).hasSize(1);
    }
}
