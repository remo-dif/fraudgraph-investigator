package com.fraudgraph.investigator.mapper;

import com.fraudgraph.investigator.domain.InvestigationCase;
import com.fraudgraph.investigator.dto.CaseDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {
    public CaseDto toDto(InvestigationCase investigationCase, List<String> alertIds) {
        return new CaseDto(
                investigationCase.getId(),
                investigationCase.getTitle(),
                investigationCase.getStatus().name(),
                investigationCase.getRiskScore(),
                investigationCase.getCreatedAt(),
                alertIds,
                investigationCase.getRecommendedActions()
        );
    }
}
