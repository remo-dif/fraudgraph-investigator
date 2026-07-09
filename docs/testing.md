# Testing Strategy

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

