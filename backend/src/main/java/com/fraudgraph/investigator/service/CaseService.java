package com.fraudgraph.investigator.service;

import com.fraudgraph.investigator.domain.CaseStatus;
import com.fraudgraph.investigator.domain.InvestigationCase;
import com.fraudgraph.investigator.dto.CaseDto;
import com.fraudgraph.investigator.dto.CreateCaseRequest;
import com.fraudgraph.investigator.exception.NotFoundException;
import com.fraudgraph.investigator.mapper.CaseMapper;
import com.fraudgraph.investigator.repository.CaseRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.context.annotation.Profile("demo")
@Service
public class CaseService {
    private static final List<String> DEFAULT_ACTIONS = List.of(
            "Freeze outbound transfers pending verification",
            "Review device and IP access history",
            "Contact customer using verified channel"
    );

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final Neo4jClient neo4jClient;

    public CaseService(CaseRepository caseRepository, CaseMapper caseMapper, Neo4jClient neo4jClient) {
        this.caseRepository = caseRepository;
        this.caseMapper = caseMapper;
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public CaseDto create(CreateCaseRequest request) {
        String caseId = "case-" + UUID.randomUUID();
        int riskScore = calculateRiskScore(request.alertIds());
        InvestigationCase investigationCase = new InvestigationCase(
                caseId,
                request.title(),
                CaseStatus.OPEN,
                riskScore,
                OffsetDateTime.now(),
                request.recommendedActions() == null || request.recommendedActions().isEmpty()
                        ? DEFAULT_ACTIONS
                        : request.recommendedActions()
        );
        caseRepository.save(investigationCase);
        linkAlerts(caseId, request.alertIds());
        return caseMapper.toDto(investigationCase, request.alertIds());
    }

    @Transactional
    public CaseDto updateStatus(String caseId, CaseStatus status) {
        InvestigationCase existing = caseRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException("Case %s was not found".formatted(caseId)));
        InvestigationCase updated = new InvestigationCase(
                existing.getId(),
                existing.getTitle(),
                status,
                existing.getRiskScore(),
                existing.getCreatedAt(),
                existing.getRecommendedActions()
        );
        caseRepository.save(updated);
        return caseMapper.toDto(updated, alertIdsFor(caseId));
    }

    private int calculateRiskScore(List<String> alertIds) {
        return neo4jClient.query("""
                        MATCH (a:Alert)
                        WHERE a.id IN $alertIds
                        RETURN toInteger(round(avg(a.riskScore))) AS riskScore
                        """)
                .bind(alertIds).to("alertIds")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("riskScore").isNull() ? 50 : record.get("riskScore").asInt())
                .one()
                .orElse(50);
    }

    private void linkAlerts(String caseId, List<String> alertIds) {
        neo4jClient.query("""
                        MATCH (c:Case {id: $caseId})
                        MATCH (a:Alert)
                        WHERE a.id IN $alertIds
                        MERGE (c)-[:CONTAINS]->(a)
                        """)
                .bind(caseId).to("caseId")
                .bind(alertIds).to("alertIds")
                .run();
    }

    private List<String> alertIdsFor(String caseId) {
        return neo4jClient.query("""
                        MATCH (:Case {id: $caseId})-[:CONTAINS]->(a:Alert)
                        RETURN a.id AS id
                        ORDER BY id
                        """)
                .bind(caseId).to("caseId")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("id").asString())
                .all()
                .stream()
                .toList();
    }
}
