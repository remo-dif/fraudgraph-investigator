package com.fraudgraph.investigator.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryDto(
        long openAlerts,
        long activeCases,
        long highRiskEntities,
        BigDecimal suspiciousAmount,
        List<PatternSummaryDto> patternSummaries
) {
}
