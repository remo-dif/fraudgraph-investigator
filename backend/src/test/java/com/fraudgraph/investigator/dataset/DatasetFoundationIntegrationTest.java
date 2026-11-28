package com.fraudgraph.investigator.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fraudgraph.investigator.FraudGraphInvestigatorApplication;
import com.fraudgraph.investigator.config.DataSeeder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.Neo4jContainer;

class DatasetFoundationIntegrationTest {
    private static final String DIGEST = "a".repeat(64);
    private static final DatasetFoundation.Event EVENT = new DatasetFoundation.Event(
            "source", "same-event", Instant.parse("2026-01-01T12:00:00Z"), CsvPreviewValidator.EventType.ACTIVITY);

    @AfterEach
    void clearActor() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void verifiesPhysicalIsolationAndScopedPersistence() throws Exception {
        try (var imported = new Neo4jContainer<>("neo4j:5.26-community").withAdminPassword("import-test-password");
             var demo = new Neo4jContainer<>("neo4j:5.24-community").withAdminPassword("fraudgraph-local-password")) {
            imported.start();
            demo.start();
            try (var demoApp = app().run("--server.port=0", "--spring.profiles.active=demo", "--spring.neo4j.uri=" + demo.getBoltUrl(),
                    "--spring.neo4j.authentication.password=fraudgraph-local-password");
                 var importApp = app().run("--server.port=0", "--spring.profiles.active=import",
                    "--IMPORT_NEO4J_URI=" + imported.getBoltUrl(), "--IMPORT_NEO4J_USERNAME=neo4j",
                    "--IMPORT_NEO4J_PASSWORD=import-test-password")) {
                assertThat(demoApp.getBeansOfType(DataSeeder.class)).hasSize(1);
                assertThat(importApp.getBeansOfType(DataSeeder.class)).isEmpty();
                assertThat(importApp.containsBean("dashboardController")).isFalse();
                assertThat(importApp.containsBean("datasetPreviewController")).isFalse();
                int port = ((ServletWebServerApplicationContext) importApp).getWebServer().getPort();
                var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/datasets/preview"))
                        .POST(HttpRequest.BodyPublishers.noBody()).build();
                assertThat(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding())
                        .statusCode()).isIn(401, 403);
                try (var wrong = GraphDatabase.driver(imported.getBoltUrl(),
                        AuthTokens.basic("neo4j", "fraudgraph-local-password"))) {
                    assertThatThrownBy(wrong::verifyConnectivity).isInstanceOf(org.neo4j.driver.exceptions.AuthenticationException.class);
                }

                var foundation = importApp.getBean(DatasetFoundation.class);
                var driver = importApp.getBean(Driver.class);
                foundation.initializeSchema();
                try (var session = driver.session()) {
                    assertThat(session.run("SHOW CONSTRAINTS YIELD name RETURN name").list(row -> row.get(0).asString()))
                            .containsExactlyInAnyOrder("dataset_id", "dataset_version_id", "imported_event_id");
                    session.run("CALL db.awaitIndexes(30)").consume();
                    assertThat(session.run("""
                            SHOW INDEXES YIELD owningConstraint, state
                            WHERE state = 'ONLINE' AND owningConstraint IS NOT NULL RETURN owningConstraint
                            """)
                            .list(row -> row.get(0).asString())).contains("imported_event_id");
                    assertThat(session.run("MATCH (n:Customer) RETURN count(n)").single().get(0).asLong()).isZero();
                }
                assertThatThrownBy(() -> DatasetFoundation.scope("a", "v1")).isInstanceOf(AccessDeniedException.class);
                actor("alice");
                var a = DatasetFoundation.scope("a", "v1");
                foundation.create(a, "1", DIGEST, DIGEST, EVENT);
                foundation.create(DatasetFoundation.scope("a", "v2"), "1", DIGEST, DIGEST, EVENT);
                assertThat(foundation.find(a, "source", "same-event")).contains(EVENT);
                assertThatThrownBy(() -> foundation.create(a, "1", DIGEST, DIGEST, EVENT))
                        .isInstanceOf(IllegalStateException.class).hasMessage("Dataset write failed");
                assertThatThrownBy(() -> DatasetFoundation.scope("", "v1")).isInstanceOf(IllegalArgumentException.class);
                var rollback = DatasetFoundation.scope("rollback", "v1");
                assertThatThrownBy(() -> foundation.create(rollback, "1", DIGEST, DIGEST, EVENT,
                        () -> { throw new IllegalStateException("Injected failure"); }))
                        .hasMessage("Injected failure");
                try (var session = driver.session()) {
                    assertThat(session.run("MATCH (n {datasetId:'rollback'}) RETURN count(n)").single().get(0).asLong()).isZero();
                }

                actor("bob");
                var b = DatasetFoundation.scope("b", "v1");
                foundation.create(b, "1", DIGEST, DIGEST, EVENT);
                var inaccessible = DatasetFoundation.scope("a", "v3");
                assertThatThrownBy(() -> foundation.create(inaccessible, "1", DIGEST, DIGEST, EVENT))
                        .isInstanceOf(AccessDeniedException.class);
                assertThat(foundation.find(DatasetFoundation.scope("a", "v1"), "source", "same-event"))
                        .isEqualTo(foundation.find(DatasetFoundation.scope("missing", "v1"), "source", "same-event")).isEmpty();
                assertThatThrownBy(() -> foundation.find(a, "source", "same-event")).isInstanceOf(AccessDeniedException.class);
                try (var session = driver.session()) {
                    session.run("""
                            MATCH (v:DatasetVersion {datasetId:'b', versionId:'v1'}), (e:Event {datasetId:'a'})
                            SET e.externalEventId = 'foreign-event'
                            CREATE (v)-[:CONTAINS_EVENT {datasetId:'b', versionId:'v1'}]->(e)
                            """).consume();
                    session.run("""
                            MATCH (v:DatasetVersion {datasetId:'b', versionId:'v1'})
                            CREATE (e:Event {datasetId:'b', versionId:'v2', namespaceKey:'source',
                                externalEventId:'wrong-version', observedAt:datetime(),eventType:'ACTIVITY'})
                            CREATE (v)-[:CONTAINS_EVENT {datasetId:'b', versionId:'v1'}]->(e)
                            """).consume();
                }
                assertThat(foundation.find(b, "source", "foreign-event")).isEmpty();
                assertThat(foundation.find(b, "source", "wrong-version")).isEmpty();
                assertThat(foundation.find(b, "source", "same-event")).contains(EVENT);
            }
        }
    }

    @Test
    void rejectsAmbiguousProfilesAndMissingImportSettings() {
        assertThatThrownBy(() -> app().run("--server.port=0", "--spring.profiles.active=demo,import"))
                .hasStackTraceContaining("Select exactly one runtime profile");
        assertThatThrownBy(() -> app().run("--server.port=0", "--spring.profiles.active=import", "--IMPORT_NEO4J_URI=",
                "--IMPORT_NEO4J_USERNAME=", "--IMPORT_NEO4J_PASSWORD="))
                .hasStackTraceContaining("Dedicated Neo4j import configuration is required");
    }

    private SpringApplicationBuilder app() {
        return new SpringApplicationBuilder(FraudGraphInvestigatorApplication.class);
    }

    private void actor(String subject) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                "test-issuer:" + subject, null, List.of(new SimpleGrantedAuthority("DATASET_INTERNAL"))));
    }
}
