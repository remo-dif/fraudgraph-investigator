package com.fraudgraph.investigator.repository;

import com.fraudgraph.investigator.dto.AlertDetailDto;
import com.fraudgraph.investigator.dto.AlertListItemDto;
import com.fraudgraph.investigator.dto.DetectionResultDto;
import com.fraudgraph.investigator.dto.EntitySummaryDto;
import com.fraudgraph.investigator.dto.GraphDto;
import com.fraudgraph.investigator.dto.PatternSummaryDto;
import com.fraudgraph.investigator.dto.SearchResultDto;
import com.fraudgraph.investigator.dto.TimelineEventDto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

@org.springframework.context.annotation.Profile("demo")
@Repository
public class GraphQueryRepository {
    private final Neo4jClient neo4jClient;

    public GraphQueryRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public long countOpenAlerts() {
        return count("MATCH (a:Alert) WHERE a.status <> 'CLOSED' RETURN count(a)");
    }

    public long countActiveCases() {
        return count("MATCH (c:Case) WHERE c.status <> 'CLOSED' RETURN count(c)");
    }

    public long countHighRiskEntities() {
        return count("MATCH (n) WHERE coalesce(n.riskScore, 0) >= 75 RETURN count(n)");
    }

    public BigDecimal suspiciousAmount() {
        return neo4jClient.query("""
                        MATCH (:Alert)-[:FLAGS]->(t:Transaction)
                        RETURN coalesce(sum(t.amount), 0) AS total
                        """)
                .fetchAs(BigDecimal.class)
                .mappedBy((typeSystem, record) -> BigDecimal.valueOf(record.get("total").asDouble()))
                .one()
                .orElse(BigDecimal.ZERO);
    }

    public List<PatternSummaryDto> patternSummaries() {
        return List.of(
                new PatternSummaryDto("shared-device", "Shared device exposure", sharedDeviceCount(), "HIGH"),
                new PatternSummaryDto("circular-transfer", "Circular money movement", circularTransferCount(), "CRITICAL"),
                new PatternSummaryDto("merchant-concentration", "Merchant concentration", highRiskMerchantCount(), "MEDIUM"),
                new PatternSummaryDto("threshold-structuring", "Sub-threshold structuring", thresholdStructuringCount(), "HIGH")
        );
    }

    public List<AlertListItemDto> listAlerts() {
        return neo4jClient.query("""
                        MATCH (alert:Alert)
                        OPTIONAL MATCH (alert)-[:FLAGS]->(tx:Transaction)
                        OPTIONAL MATCH (acct:Account)-[:INITIATED]->(tx)
                        RETURN alert, tx, acct
                        ORDER BY alert.createdAt DESC
                        """)
                .fetchAs(AlertListItemDto.class)
                .mappedBy((typeSystem, record) -> {
                    Node alert = record.get("alert").asNode();
                    Node entity = record.get("acct").isNull() ? record.get("tx").asNode() : record.get("acct").asNode();
                    return new AlertListItemDto(
                            alert.get("id").asString(),
                            alert.get("title").asString(),
                            alert.get("status").asString(),
                            alert.get("severity").asString(),
                            alert.get("riskScore").asInt(),
                            entity.get("id").asString(),
                            labelFor(entity),
                            offset(alert.get("createdAt").asString())
                    );
                })
                .all()
                .stream()
                .toList();
    }

    public AlertDetailDto getAlertDetail(String alertId) {
        return neo4jClient.query("""
                        MATCH (alert:Alert {id: $alertId})-[:FLAGS]->(tx:Transaction)
                        OPTIONAL MATCH (acct:Account)-[:INITIATED]->(tx)
                        OPTIONAL MATCH (tx)-[:SENT_TO]->(merchant:Merchant)
                        RETURN alert, tx, acct, merchant
                        """)
                .bind(alertId).to("alertId")
                .fetchAs(AlertDetailDto.class)
                .mappedBy((typeSystem, record) -> {
                    Node alert = record.get("alert").asNode();
                    Node tx = record.get("tx").asNode();
                    List<EntitySummaryDto> related = new ArrayList<>();
                    related.add(summary(tx));
                    if (!record.get("acct").isNull()) {
                        related.add(summary(record.get("acct").asNode()));
                    }
                    if (!record.get("merchant").isNull()) {
                        related.add(summary(record.get("merchant").asNode()));
                    }
                    return new AlertDetailDto(
                            alert.get("id").asString(),
                            alert.get("title").asString(),
                            alert.get("status").asString(),
                            alert.get("severity").asString(),
                            alert.get("riskScore").asInt(),
                            alert.get("description").asString(),
                            offset(alert.get("createdAt").asString()),
                            alert.get("riskReasons").asList(v -> v.asString()),
                            List.of(transactionTimeline(tx, record.get("merchant").isNull() ? null : record.get("merchant").asNode())),
                            related,
                            graphNeighborhood(tx.get("id").asString(), 2),
                            alert.get("recommendedActions").asList(v -> v.asString())
                    );
                })
                .one()
                .orElseThrow();
    }

    public List<SearchResultDto> searchEntities(String term) {
        return neo4jClient.query("""
                        MATCH (n)
                        WHERE any(label IN labels(n) WHERE label IN ['Customer','Account','Card','Device','IPAddress','Merchant','Transaction','Alert','Case'])
                          AND (
                            toLower(coalesce(n.id, '')) CONTAINS toLower($term)
                            OR toLower(coalesce(n.name, '')) CONTAINS toLower($term)
                            OR toLower(coalesce(n.email, '')) CONTAINS toLower($term)
                            OR toLower(coalesce(n.accountNumber, '')) CONTAINS toLower($term)
                            OR toLower(coalesce(n.title, '')) CONTAINS toLower($term)
                          )
                        RETURN n
                        ORDER BY coalesce(n.riskScore, 0) DESC, coalesce(n.name, n.id)
                        LIMIT 25
                        """)
                .bind(term).to("term")
                .fetchAs(SearchResultDto.class)
                .mappedBy((typeSystem, record) -> {
                    Node node = record.get("n").asNode();
                    return new SearchResultDto(
                            node.get("id").asString(),
                            typeFor(node),
                            labelFor(node),
                            riskScore(node),
                            subtitleFor(node)
                    );
                })
                .all()
                .stream()
                .toList();
    }

    public GraphDto graphNeighborhood(String entityId, int depth) {
        int cappedDepth = Math.max(1, Math.min(depth, 3));
        String query = """
                MATCH (center {id: $entityId})
                MATCH path = (center)-[*1..%d]-(neighbor)
                WHERE all(n IN nodes(path) WHERE any(label IN labels(n) WHERE label IN ['Customer','Account','Card','Device','IPAddress','Merchant','Transaction','Alert','Case']))
                RETURN nodes(path) AS nodes, relationships(path) AS rels
                LIMIT 150
                """.formatted(cappedDepth);

        Map<String, GraphDto.GraphNodeDto> nodes = new LinkedHashMap<>();
        Map<String, GraphDto.GraphEdgeDto> edges = new LinkedHashMap<>();
        neo4jClient.query(query)
                .bind(entityId).to("entityId")
                .fetch()
                .all()
                .forEach(row -> {
                    @SuppressWarnings("unchecked")
                    List<Node> rowNodes = (List<Node>) row.get("nodes");
                    @SuppressWarnings("unchecked")
                    List<Relationship> rowRels = (List<Relationship>) row.get("rels");
                    rowNodes.forEach(node -> nodes.putIfAbsent(node.get("id").asString(), graphNode(node)));
                    rowRels.forEach(rel -> {
                        String source = idForInternalNode(rowNodes, rel.startNodeId());
                        String target = idForInternalNode(rowNodes, rel.endNodeId());
                        if (source != null && target != null) {
                            edges.putIfAbsent(String.valueOf(rel.id()), graphEdge(rel, source, target));
                        }
                    });
                });
        return new GraphDto(new ArrayList<>(nodes.values()), new ArrayList<>(edges.values()));
    }

    public List<DetectionResultDto> detectSuspiciousPatterns() {
        List<DetectionResultDto> results = new ArrayList<>();
        results.addAll(sharedDevicePatterns());
        results.addAll(circularTransferPatterns());
        results.addAll(highRiskMerchantPatterns());
        results.addAll(connectedAccountPatterns());
        results.addAll(suspiciousClusterPatterns());
        return results;
    }

    public List<DetectionResultDto> sharedDevicePatterns() {
        // Finds devices used by three or more accounts, a strong signal of mule-account coordination.
        return neo4jClient.query("""
                        MATCH (device:Device)<-[:LOGGED_IN_FROM]-(account:Account)
                        WITH device, collect(account) AS accounts
                        WHERE size(accounts) >= 3
                        RETURN device, accounts
                        ORDER BY size(accounts) DESC
                        """)
                .fetchAs(DetectionResultDto.class)
                .mappedBy((typeSystem, record) -> {
                    Node device = record.get("device").asNode();
                    List<Node> accounts = record.get("accounts").asList(v -> v.asNode());
                    return new DetectionResultDto(
                            "shared-device",
                            "HIGH",
                            "%s is shared by %d accounts".formatted(labelFor(device), accounts.size()),
                            ids(accounts),
                            List.of("Multiple accounts authenticated from the same device", "Device risk score is " + riskScore(device))
                    );
                })
                .all()
                .stream()
                .toList();
    }

    public List<DetectionResultDto> circularTransferPatterns() {
        // Detects 3-account payment loops where funds move around a cycle within the transaction graph.
        return neo4jClient.query("""
                        MATCH p=(a1:Account)-[:INITIATED]->(:Transaction)-[:TRANSFERRED_TO]->(a2:Account)
                              -[:INITIATED]->(:Transaction)-[:TRANSFERRED_TO]->(a3:Account)
                              -[:INITIATED]->(:Transaction)-[:TRANSFERRED_TO]->(a1)
                        WHERE a1.id < a2.id AND a2.id < a3.id
                        RETURN a1, a2, a3
                        LIMIT 10
                        """)
                .fetchAs(DetectionResultDto.class)
                .mappedBy((typeSystem, record) -> {
                    List<String> ids = List.of(
                            record.get("a1").asNode().get("id").asString(),
                            record.get("a2").asNode().get("id").asString(),
                            record.get("a3").asNode().get("id").asString()
                    );
                    return new DetectionResultDto(
                            "circular-transfer",
                            "CRITICAL",
                            "Circular transfer loop across " + String.join(", ", ids),
                            ids,
                            List.of("Funds return to the origin account through intermediary accounts", "Pattern is consistent with layering")
                    );
                })
                .all()
                .stream()
                .toList();
    }

    public List<DetectionResultDto> highRiskMerchantPatterns() {
        // Identifies merchants receiving unusually concentrated volume from many accounts.
        return neo4jClient.query("""
                        MATCH (account:Account)-[:INITIATED]->(tx:Transaction)-[:SENT_TO]->(merchant:Merchant)
                        WITH merchant, count(tx) AS txCount, count(DISTINCT account) AS accountCount, sum(tx.amount) AS totalAmount
                        WHERE txCount >= 12 OR accountCount >= 8
                        RETURN merchant, txCount, accountCount, totalAmount
                        ORDER BY txCount DESC
                        """)
                .fetchAs(DetectionResultDto.class)
                .mappedBy((typeSystem, record) -> {
                    Node merchant = record.get("merchant").asNode();
                    return new DetectionResultDto(
                            "high-risk-merchant",
                            "MEDIUM",
                            "%s received %d transactions from %d accounts".formatted(
                                    labelFor(merchant),
                                    record.get("txCount").asLong(),
                                    record.get("accountCount").asLong()
                            ),
                            List.of(merchant.get("id").asString()),
                            List.of("Merchant concentration exceeds baseline", "Aggregate exposure $" + BigDecimal.valueOf(record.get("totalAmount").asDouble()))
                    );
                })
                .all()
                .stream()
                .toList();
    }

    public List<DetectionResultDto> connectedAccountPatterns() {
        // Finds accounts within two graph hops of entities that were already marked suspicious.
        return neo4jClient.query("""
                        MATCH (suspicious)
                        WHERE coalesce(suspicious.previouslySuspicious, false) = true OR coalesce(suspicious.riskScore, 0) >= 85
                        MATCH path=(suspicious)-[*1..2]-(account:Account)
                        WITH account, collect(DISTINCT suspicious.id) AS suspiciousIds, count(path) AS pathCount
                        WHERE pathCount >= 1
                        RETURN account, suspiciousIds, pathCount
                        ORDER BY pathCount DESC
                        LIMIT 15
                        """)
                .fetchAs(DetectionResultDto.class)
                .mappedBy((typeSystem, record) -> {
                    Node account = record.get("account").asNode();
                    List<String> suspiciousIds = record.get("suspiciousIds").asList(v -> v.asString());
                    return new DetectionResultDto(
                            "connected-within-2-hops",
                            "HIGH",
                            labelFor(account) + " is within 2 hops of suspicious entities",
                            prepend(account.get("id").asString(), suspiciousIds),
                            List.of("Graph proximity to prior suspicious entity", "Path count: " + record.get("pathCount").asLong())
                    );
                })
                .all()
                .stream()
                .toList();
    }

    public List<DetectionResultDto> suspiciousClusterPatterns() {
        // Clusters accounts, devices, IP addresses, and merchants around flagged transactions.
        return neo4jClient.query("""
                        MATCH (alert:Alert)-[:FLAGS]->(tx:Transaction)<-[:INITIATED]-(account:Account)
                        OPTIONAL MATCH (account)-[:LOGGED_IN_FROM]->(device:Device)-[:USED_IP]->(ip:IPAddress)
                        OPTIONAL MATCH (tx)-[:SENT_TO]->(merchant:Merchant)
                        WITH account, collect(DISTINCT alert.id) AS alerts, collect(DISTINCT device.id) AS devices,
                             collect(DISTINCT ip.id) AS ips, collect(DISTINCT merchant.id) AS merchants
                        WHERE size(alerts) >= 1 AND (size(devices) >= 1 OR size(merchants) >= 1)
                        RETURN account, alerts, devices, ips, merchants
                        LIMIT 20
                        """)
                .fetchAs(DetectionResultDto.class)
                .mappedBy((typeSystem, record) -> {
                    Node account = record.get("account").asNode();
                    List<String> entityIds = new ArrayList<>();
                    entityIds.add(account.get("id").asString());
                    entityIds.addAll(record.get("alerts").asList(v -> v.asString()));
                    entityIds.addAll(record.get("devices").asList(v -> v.asString()));
                    entityIds.addAll(record.get("ips").asList(v -> v.asString()));
                    entityIds.addAll(record.get("merchants").asList(v -> v.asString()));
                    return new DetectionResultDto(
                            "suspicious-transaction-cluster",
                            "HIGH",
                            "Flagged transaction cluster around " + labelFor(account),
                            entityIds,
                            List.of("Alerted transactions share infrastructure or merchant endpoints", "Cluster contains " + entityIds.size() + " distinct entities")
                    );
                })
                .all()
                .stream()
                .toList();
    }

    private long sharedDeviceCount() {
        return count("""
                MATCH (device:Device)<-[:LOGGED_IN_FROM]-(account:Account)
                WITH device, count(account) AS accounts
                WHERE accounts >= 3
                RETURN count(device)
                """);
    }

    private long circularTransferCount() {
        return count("""
                MATCH (a1:Account)-[:INITIATED]->(:Transaction)-[:TRANSFERRED_TO]->(a2:Account)
                      -[:INITIATED]->(:Transaction)-[:TRANSFERRED_TO]->(a3:Account)
                      -[:INITIATED]->(:Transaction)-[:TRANSFERRED_TO]->(a1)
                WHERE a1.id < a2.id AND a2.id < a3.id
                RETURN count(*)
                """);
    }

    private long highRiskMerchantCount() {
        return count("""
                MATCH (:Account)-[:INITIATED]->(tx:Transaction)-[:SENT_TO]->(merchant:Merchant)
                WITH merchant, count(tx) AS txCount
                WHERE txCount >= 12
                RETURN count(merchant)
                """);
    }

    private long thresholdStructuringCount() {
        return count("""
                MATCH (:Account)-[:INITIATED]->(tx:Transaction)
                WHERE tx.amount >= 9000 AND tx.amount < 10000
                RETURN count(tx)
                """);
    }

    private long count(String cypher) {
        return neo4jClient.query(cypher)
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get(0).asLong())
                .one()
                .orElse(0L);
    }

    private TimelineEventDto transactionTimeline(Node tx, Node merchant) {
        String target = merchant == null ? "unknown counterparty" : labelFor(merchant);
        return new TimelineEventDto(
                offset(tx.get("occurredAt").asString()),
                tx.get("type").asString(),
                "Transaction " + tx.get("id").asString() + " sent to " + target,
                BigDecimal.valueOf(tx.get("amount").asDouble()),
                tx.get("currency").asString()
        );
    }

    private EntitySummaryDto summary(Node node) {
        return new EntitySummaryDto(node.get("id").asString(), typeFor(node), labelFor(node), riskScore(node), properties(node));
    }

    private GraphDto.GraphNodeDto graphNode(Node node) {
        return new GraphDto.GraphNodeDto(node.get("id").asString(), typeFor(node), labelFor(node), riskScore(node), properties(node));
    }

    private GraphDto.GraphEdgeDto graphEdge(Relationship relationship, String source, String target) {
        return new GraphDto.GraphEdgeDto(String.valueOf(relationship.id()), source, target, relationship.type(), relationship.asMap());
    }

    private Map<String, Object> properties(Node node) {
        Map<String, Object> values = new LinkedHashMap<>(node.asMap());
        values.remove("riskReasons");
        values.remove("recommendedActions");
        return values;
    }

    private String typeFor(Node node) {
        return node.labels().iterator().next().replace("IPAddress", "IP_ADDRESS").toUpperCase();
    }

    private String labelFor(Node node) {
        Map<String, Object> properties = node.asMap();
        Object value = properties.getOrDefault("name",
                properties.getOrDefault("title",
                        properties.getOrDefault("accountNumber",
                                properties.getOrDefault("maskedPan",
                                        properties.getOrDefault("ip", properties.get("id"))))));
        return String.valueOf(value);
    }

    private String subtitleFor(Node node) {
        Map<String, Object> properties = node.asMap();
        if (properties.containsKey("email")) {
            return String.valueOf(properties.get("email"));
        }
        if (properties.containsKey("status")) {
            return "Status: " + properties.get("status");
        }
        if (properties.containsKey("country")) {
            return "Country: " + properties.get("country");
        }
        return typeFor(node);
    }

    private Integer riskScore(Node node) {
        return node.containsKey("riskScore") ? node.get("riskScore").asInt() : null;
    }

    private OffsetDateTime offset(String value) {
        return OffsetDateTime.parse(value);
    }

    private List<String> ids(Collection<Node> nodes) {
        return nodes.stream().map(node -> node.get("id").asString()).toList();
    }

    private List<String> prepend(String first, List<String> rest) {
        Set<String> ids = new LinkedHashSet<>();
        ids.add(first);
        ids.addAll(rest);
        return new ArrayList<>(ids);
    }

    private String idForInternalNode(List<Node> nodes, long internalId) {
        return nodes.stream()
                .filter(node -> node.id() == internalId)
                .findFirst()
                .map(node -> node.get("id").asString())
                .orElse(null);
    }
}
