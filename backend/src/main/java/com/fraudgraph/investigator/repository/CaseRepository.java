package com.fraudgraph.investigator.repository;

import com.fraudgraph.investigator.domain.InvestigationCase;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface CaseRepository extends Neo4jRepository<InvestigationCase, String> {
}
