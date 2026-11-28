# Testing Strategy

## Required imported-data gate

The `Neo4j Community integration` CI job runs the actual repository and two Community instances through Testcontainers. It executes before the existing demo smoke. Configure this job as a required status check in the repository ruleset. Reproduce locally with Java 21, Gradle 8.14.3 and an accessible Docker daemon:

```bash
docker info
gradle :backend:test :backend:neo4jIntegrationTest --no-daemon
docker compose config --quiet
docker compose -p fraudgraph-import -f docker-compose.import.yml config --quiet
```

The import Compose validation requires a dedicated `IMPORT_NEO4J_PASSWORD`. Missing Docker, zero tests, skipped tests and failures fail CI. The real multipart and end-to-end ingestion journeys below remain planned.

| Phase | Mandatory behavior |
| --- | --- |
| 1A | Clean schema initialization and rerun, composite uniqueness, application missing-key rejection, scoped reads/writes, overlapping dataset IDs, mismatched relationship rejection, server-derived actor policy and anonymous/wrong-owner denial, transaction rollback. |
| 1B | Concurrent identical imports, corrected version preservation, aborted import and retry, publication/delete races, revoked access and repeatable deletion. Embedded HTTP server tests verify real multipart limits, excess part count, malformed input, unauthorized admission, concurrent budgets and temporary file cleanup. |
| 1C | Row/field/timestamp provenance, benign device and NAT fixtures, injected cross-scope edge exclusion, index seek plan, bounded hub, deadline/truncation and redacted output. E2E selects the synthetic fixture, validates and imports it, searches a device, asserts dated related evidence, downloads JSON, deletes and verifies search and export are revoked. |

The existing sections below describe the demo test baseline and demo journey.

## Backend

Current tests cover service composition and source-level query presence. The next quality step is behavior testing against Neo4j.

Recommended coverage:

- Unit tests for application services, mappers, risk scoring, and pattern detection.
- Neo4j Testcontainers tests for Cypher repositories.
- Controller tests for validation and error responses.
- API contract tests from generated OpenAPI output.

## Frontend

Current tests cover reusable UI behavior. The next quality step is workflow coverage.

Recommended coverage:

- Component tests for filters, details panels, and selection state.
- Hook tests for graph and hypergraph selection/filtering.
- Route tests for dashboard, alerts, graph explorer, and hypergraph explorer.
- Playwright e2e flow from dashboard to alert to case creation to hypergraph inspection.

## E2E Scenario

1. Start `docker compose up --build`.
2. Open dashboard and verify seeded alert metrics.
3. Navigate to Alerts and open `alert-001`.
4. Create a case.
5. Open Hypergraph Explorer for `case-seed-001`.
6. Verify shared device and merchant burst clusters are highlighted.

