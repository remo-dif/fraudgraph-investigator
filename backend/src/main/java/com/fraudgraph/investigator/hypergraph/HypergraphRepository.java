package com.fraudgraph.investigator.hypergraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.types.Node;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@Repository
public class HypergraphRepository {
    private final Neo4jClient neo4jClient;
    private final HypergraphMapper mapper;

    public HypergraphRepository(Neo4jClient neo4jClient, HypergraphMapper mapper) {
        this.neo4jClient = neo4jClient;
        this.mapper = mapper;
    }

    public HypergraphDTO findCaseHypergraph(String caseId) {
        return queryHypergraph("""
                MATCH (:Case {id: $id})-[:CONTAINS]->(:Alert)-[:FLAGS]->(:Transaction)<-[:DERIVED_FROM]-(event)
                WHERE event:PaymentEvent OR event:AlertEvent
                MATCH (participant)-[:PARTICIPATED_IN]->(event)
                RETURN event, collect(DISTINCT participant) AS participants
                ORDER BY event.timestamp DESC
                LIMIT 80
                """, caseId);
    }

    public HypergraphDTO findEntityNeighborhood(String entityId) {
        return queryHypergraph("""
                MATCH (center {id: $id})-[:PARTICIPATED_IN]->(event)<-[:PARTICIPATED_IN]-(participant)
                WHERE event:PaymentEvent OR event:LoginEvent OR event:DeviceFingerprintEvent OR event:AlertEvent
                RETURN event, collect(DISTINCT participant) AS participants
                ORDER BY event.timestamp DESC
                LIMIT 100
                """, entityId);
    }

    public HypergraphDTO findEvent(String eventId) {
        return queryHypergraph("""
                MATCH (event {id: $id})<-[:PARTICIPATED_IN]-(participant)
                WHERE event:PaymentEvent OR event:LoginEvent OR event:DeviceFingerprintEvent OR event:AlertEvent
                RETURN event, collect(DISTINCT participant) AS participants
                LIMIT 1
                """, eventId);
    }

    public HypergraphDTO findSuspiciousClusterCandidateGraph() {
        return queryHypergraph("""
                MATCH (event)<-[:PARTICIPATED_IN]-(participant)
                WHERE (event:PaymentEvent OR event:LoginEvent OR event:DeviceFingerprintEvent OR event:AlertEvent)
                  AND coalesce(event.riskScore, 0) >= 70
                RETURN event, collect(DISTINCT participant) AS participants
                ORDER BY event.riskScore DESC, event.timestamp DESC
                LIMIT 120
                """, "");
    }

    private HypergraphDTO queryHypergraph(String cypher, String id) {
        Map<String, HyperNodeDTO> nodes = new LinkedHashMap<>();
        Map<String, HyperEdgeDTO> edges = new LinkedHashMap<>();

        Neo4jClient.RunnableSpec query = neo4jClient.query(cypher);
        if (!id.isBlank()) {
            query = query.bind(id).to("id");
        }

        query.fetch().all().forEach(row -> {
            Node event = (Node) row.get("event");
            @SuppressWarnings("unchecked")
            List<Node> participants = (List<Node>) row.get("participants");
            List<HyperNodeDTO> participantDtos = new ArrayList<>();
            for (Node participant : participants) {
                HyperNodeDTO dto = mapper.toNode(participant);
                nodes.putIfAbsent(dto.id(), dto);
                participantDtos.add(dto);
            }
            HyperEdgeDTO edge = mapper.toEdge(event, participantDtos);
            edges.putIfAbsent(edge.id(), edge);
        });

        return new HypergraphDTO(new ArrayList<>(nodes.values()), new ArrayList<>(edges.values()), List.of());
    }
}
