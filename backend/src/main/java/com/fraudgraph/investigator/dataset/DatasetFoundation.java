package com.fraudgraph.investigator.dataset;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.neo4j.driver.Driver;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.exceptions.Neo4jException;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

@Repository
@Profile("import")
class DatasetFoundation {
    private static final TransactionConfig DEADLINE = TransactionConfig.builder()
            .withTimeout(Duration.ofSeconds(10)).build();
    private final Driver driver;

    DatasetFoundation(Driver driver) {
        this.driver = driver;
    }

    void initializeSchema() {
        try (var session = driver.session()) {
            for (String statement : new String[] {
                    "CREATE CONSTRAINT dataset_id IF NOT EXISTS FOR (d:Dataset) REQUIRE d.datasetId IS UNIQUE",
                    """
                    CREATE CONSTRAINT dataset_version_id IF NOT EXISTS FOR (v:DatasetVersion)
                    REQUIRE (v.datasetId, v.versionId) IS UNIQUE
                    """,
                    """
                    CREATE CONSTRAINT imported_event_id IF NOT EXISTS FOR (e:Event)
                    REQUIRE (e.datasetId, e.versionId, e.namespaceKey, e.externalEventId) IS UNIQUE
                    """
            }) {
                session.run(statement, DEADLINE).consume();
            }
        }
    }

    static Scope scope(String datasetId, String versionId) {
        var actor = SecurityContextHolder.getContext().getAuthentication();
        if (actor == null || !actor.isAuthenticated() || actor.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("DATASET_INTERNAL"))) {
            throw new AccessDeniedException("Dataset unavailable");
        }
        return new Scope(required(actor.getName()), required(datasetId), required(versionId));
    }

    void create(Scope scope, String contractVersion, String contentDigest, String mappingDigest, Event event) {
        create(scope, contractVersion, contentDigest, mappingDigest, event, () -> {});
    }

    void create(Scope scope, String contractVersion, String contentDigest, String mappingDigest,
                Event event, Runnable beforeCommit) {
        var parameters = new java.util.HashMap<>(parameters(scope, event.namespaceKey(), event.externalEventId()));
        parameters.putAll(Map.of("contract", required(contractVersion), "digest", digest(contentDigest),
                "mapping", digest(mappingDigest), "observed", event.observedAt().toString(),
                "type", event.type().name(), "created", Instant.now().toString()));
        try (var session = driver.session(); var tx = session.beginTransaction(DEADLINE)) {
            tx.run("""
                    MERGE (d:Dataset {datasetId: $dataset})
                    ON CREATE SET d.ownerActorKey = $actor, d.lifecycleState = 'ACTIVE',
                                  d.createdAt = datetime($created)
                    SET d.lifecycleState = d.lifecycleState
                    WITH d WHERE d.ownerActorKey = $actor AND d.lifecycleState = 'ACTIVE'
                    CREATE (v:DatasetVersion {datasetId: $dataset, versionId: $version,
                        contractVersion: $contract, contentDigest: $digest, mappingDigest: $mapping,
                        state: 'PUBLISHED', createdAt: datetime($created)})
                    CREATE (e:Event {datasetId: $dataset, versionId: $version, namespaceKey: $namespace,
                        externalEventId: $event, observedAt: datetime($observed), eventType: $type})
                    CREATE (d)-[:HAS_VERSION {datasetId: $dataset, versionId: $version}]->(v)
                    CREATE (v)-[:CONTAINS_EVENT {datasetId: $dataset, versionId: $version}]->(e)
                    RETURN v.versionId AS id
                    """, parameters).list().stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("Dataset unavailable"));
            beforeCommit.run();
            tx.commit();
        } catch (Neo4jException failure) {
            throw new IllegalStateException("Dataset write failed");
        }
    }

    Optional<Event> find(Scope scope, String namespace, String id) {
        try (var session = driver.session(); var tx = session.beginTransaction(DEADLINE)) {
            var result = tx.run("""
                    MATCH (d:Dataset {datasetId: $dataset, ownerActorKey: $actor, lifecycleState: 'ACTIVE'})
                        -[:HAS_VERSION {datasetId: $dataset, versionId: $version}]->
                        (v:DatasetVersion {datasetId: $dataset, versionId: $version, state: 'PUBLISHED'})
                        -[:CONTAINS_EVENT {datasetId: $dataset, versionId: $version}]->
                        (e:Event {datasetId: $dataset, versionId: $version,
                            namespaceKey: $namespace, externalEventId: $event})
                    RETURN DISTINCT e.namespaceKey AS namespace, e.externalEventId AS id,
                                    e.observedAt AS observed, e.eventType AS type
                    LIMIT 1
                    """, parameters(scope, namespace, id)).list();
            tx.commit();
            return result.stream().findFirst().map(row -> new Event(row.get("namespace").asString(),
                    row.get("id").asString(), row.get("observed").asOffsetDateTime().toInstant(),
                    CsvPreviewValidator.EventType.valueOf(row.get("type").asString())));
        } catch (Neo4jException failure) {
            throw new IllegalStateException("Dataset read failed");
        }
    }

    private static Map<String, Object> parameters(Scope scope, String namespace, String id) {
        Scope current = scope(scope.datasetId, scope.versionId);
        if (!current.actor.equals(scope.actor)) {
            throw new AccessDeniedException("Dataset unavailable");
        }
        return Map.of("actor", scope.actor, "dataset", scope.datasetId, "version", scope.versionId,
                "namespace", required(namespace), "event", required(id));
    }

    private static String required(String value) {
        if (value == null || value.isBlank() || value.length() > 256
                || value.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid dataset metadata");
        }
        return value;
    }

    private static String digest(String value) {
        if (!required(value).matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Invalid dataset metadata");
        }
        return value;
    }

    static final class Scope {
        private final String actor;
        private final String datasetId;
        private final String versionId;

        private Scope(String actor, String datasetId, String versionId) {
            this.actor = actor;
            this.datasetId = datasetId;
            this.versionId = versionId;
        }
    }

    record Event(String namespaceKey, String externalEventId, Instant observedAt, CsvPreviewValidator.EventType type) {
        Event {
            required(namespaceKey);
            required(externalEventId);
            if (observedAt == null || type == null) {
                throw new IllegalArgumentException("Invalid event metadata");
            }
        }
    }
}
