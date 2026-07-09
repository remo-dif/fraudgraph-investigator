# Architecture Assessment

This assessment captures the current state of FraudGraph Investigator after the initial full-stack implementation and before the hypergraph refactoring is complete. The goal is incremental improvement, not a rewrite.

## Current Architecture

The repository is organized as a Docker Compose application with:

- `backend`: Spring Boot API using Spring Data Neo4j and `Neo4jClient`.
- `frontend`: React + TypeScript + TanStack Query + React Flow.
- `neo4j`: local database runtime notes.
- `docs`: architecture and refactoring documentation.

The current application is runnable and demonstrates graph-based fraud investigation, but the first version still uses broad technical layers (`controller`, `service`, `repository`, `dto`) instead of deeper feature/application boundaries.

## Issue Classification

### Critical

- No authentication or authorization yet. This is acceptable for local portfolio startup, but not for a production fraud investigation system.
- Hypergraph abstraction is not yet the primary investigation model. Neo4j graph queries exist, but event-centric hyperedges must be formalized.

### High

- Backend package structure is technical-layer oriented. A future clean architecture pass should introduce `api`, `application`, `domain`, `infrastructure`, `common`, and feature-specific packages.
- Frontend pages currently live mostly under `pages` and `components`; feature modules should isolate dashboard, alerts, graph, hypergraph, search, and investigations.
- Repository tests are lightweight. Cypher behavior should be verified with Neo4j Testcontainers.
- Error responses are consistent but currently expose raw exception messages for unexpected server errors.

### Medium

- Detection rules are synchronous request-time computations; production detection should run as jobs with persisted findings.
- Graph layout is deterministic but simple; larger investigations need a dedicated layout engine and possibly worker-based computation.
- CORS is configured for local development only.
- No structured application logging or correlation IDs.

### Low

- Docker frontend image uses Vite dev server for local development; a production deployment would use a static asset server.
- Screenshot documentation is placeholder-only until the app is run and captured.
- Some UI behavior is intentionally simple, such as graph expansion depth and filter persistence.

## Technical Debt

- Move from broad package layers to feature/application modules.
- Add JWT or OIDC-based authentication.
- Replace request-time detection with persisted detection runs.
- Add integration tests for Neo4j Cypher and Docker Compose smoke tests.
- Add frontend route-level lazy loading and error boundaries.
- Add accessibility and keyboard interaction tests for graph workflows.

## Recommended Pull Request Sequence

1. **PR1: Architecture documentation and ADRs**
   - Add assessment, roadmap, and ADRs.
   - No runtime behavior changes.

2. **PR2: Backend hypergraph API**
   - Add `hypergraph` package.
   - Expose event-centric DTOs while keeping Neo4j hidden.
   - Add scorer, detector, mapper, and service tests.

3. **PR3: Hypergraph seed/model enrichment**
   - Add event nodes such as `PaymentEvent`, `LoginEvent`, `AlertEvent`, and `InvestigationCase`.
   - Connect participating entities via `PARTICIPATED_IN`.

4. **PR4: Frontend hypergraph feature module**
   - Add `features/hypergraph`.
   - Add explorer route, filters, details panel, selection, and layout hooks.

5. **PR5: Frontend feature modularization**
   - Move existing dashboard, alerts, graph, search, and investigation pages into feature modules.
   - Introduce route-level lazy loading.

6. **PR6: Test hardening**
   - Add Neo4j Testcontainers.
   - Add Playwright investigation and hypergraph explorer flows.

7. **PR7: Security foundation**
   - Add authentication/authorization boundaries.
   - Harden CORS, validation, and error leakage.

8. **PR8: Performance and observability**
   - Add structured logs, request IDs, graph rendering optimizations, and worker-ready layout abstractions.

