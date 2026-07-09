# Pull Request Roadmap

FraudGraph Investigator should evolve through small production-ready PRs. Each PR should compile, pass tests, and preserve local `docker compose up --build`.

## PR1: Architecture Assessment and ADRs

Scope:

- Architecture assessment.
- Hypergraph ADR.
- Initial technical debt report.
- Refactoring roadmap.

Acceptance:

- Documentation is accurate and references current implementation.
- No runtime behavior changes.

## PR2: Backend Hypergraph API

Scope:

- Add `hypergraph` backend package.
- Add frontend-neutral DTOs for hypernodes, hyperedges, and suspicious hypergraph patterns.
- Add service, repository, mapper, detector, scorer, and controller.
- Add unit tests for mapper, scorer, detector, and service behavior.

Acceptance:

- `GET /api/hypergraph/cases/{caseId}` returns event-centric investigation context.
- `GET /api/hypergraph/entities/{entityId}/neighborhood` returns hyperedges and participating entities.
- `GET /api/hypergraph/events/{eventId}` returns a single hyperedge neighborhood.
- `GET /api/hypergraph/patterns/suspicious-clusters` returns at least three hypergraph suspicious pattern classes.

## PR3: Neo4j Hyperedge Persistence Enrichment

Scope:

- Represent `PaymentEvent`, `LoginEvent`, `DeviceFingerprintEvent`, and `AlertEvent` as Neo4j event nodes.
- Add `PARTICIPATED_IN` relationships.
- Add constraints and indexes for event nodes.
- Keep existing pairwise graph model for compatibility.

Acceptance:

- Frontend receives no Neo4j-specific implementation details.
- Seed data remains deterministic enough for tests and demos.

## PR4: Frontend Hypergraph Feature Module

Scope:

- Add `features/hypergraph`.
- Add route for Hypergraph Explorer.
- Add filters, legend, details panel, selection state, and layout hook.
- Use React Flow as the first rendering engine.

Acceptance:

- UI answers what happened, who participated, why suspicious, and what to inspect next.
- Tests cover filtering and selection.

## PR5: Frontend Feature Modularization

Scope:

- Move existing pages into feature modules.
- Add route-level lazy loading.
- Centralize shared UI and typed API hooks.

Acceptance:

- No behavior regressions.
- Bundle size does not grow materially.

## PR6: Backend Clean Architecture Split

Scope:

- Split broad `GraphQueryRepository` into bounded repositories.
- Move mapping out of repositories.
- Isolate application services from infrastructure.

Acceptance:

- Controllers depend on application services and DTOs only.
- Repository tests cover Cypher behavior.

## PR7: Security Baseline

Scope:

- Add Spring Security.
- Add local analyst role model.
- Add safe error responses.
- Add audit events for case actions.

Acceptance:

- Mutating case endpoints require authorization.
- Unexpected exceptions do not leak implementation details.

## PR8: Test and CI Hardening

Scope:

- Add Neo4j Testcontainers.
- Add Playwright flows.
- Add CI build, test, Docker build, and dependency scan.

Acceptance:

- CI catches backend, frontend, Docker, and e2e regressions.

