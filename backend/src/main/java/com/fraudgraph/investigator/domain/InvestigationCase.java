package com.fraudgraph.investigator.domain;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Case")
public class InvestigationCase {
    @Id
    private String id;
    private String title;
    private CaseStatus status;
    private Integer riskScore;
    @Property("createdAt")
    private OffsetDateTime createdAt;
    private List<String> recommendedActions;

    public InvestigationCase() {
    }

    public InvestigationCase(String id, String title, CaseStatus status, Integer riskScore, OffsetDateTime createdAt, List<String> recommendedActions) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.riskScore = riskScore;
        this.createdAt = createdAt;
        this.recommendedActions = recommendedActions;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }
}
