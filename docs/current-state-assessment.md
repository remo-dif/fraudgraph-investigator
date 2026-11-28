# Current State Assessment

Baseline: branch main, commit `2145aed`. No AGENTS.md or CLAUDE.md appeared in the checkout inventory. Existing architecture, security, testing and CI documents are historical context. The checked-in code determines current capabilities.

| Area | Verified in the checkout | Reuse and limitations |
| --- | --- | --- |
| Backend | Controllers and services under `backend/src/main/java/com/fraudgraph/investigator`, with DTOs, `CaseService`, `CaseRepository` and `GraphQueryRepository` | Reuse API layering and case workflow concepts. The graph repository combines dashboard, alerts, search, traversal and detection. |
| Search and graph | `GraphQueryRepository.java:145` caps search at 25. Its neighborhood query clamps depth to 1–3 and limits returned paths to 150. User values are bound. | Reuse graph DTO and visualization concepts. A result cap does not bound traversal cost. Current queries have no dataset scope or explicit traversal timeout. |
| Hypergraph | `config/DataSeeder.java:266` creates payment, login and alert event nodes with participation links. `hypergraph/HypergraphRepository.java` projects event participants to DTOs. | Reuse incidence modeling and separate rendering. Payment context is derived from account-level links, so it cannot prove which device participated in a specific historical payment. Alert projections can repeat payment evidence. |
| Detection | `hypergraph/HypergraphPatternDetector.java` uses shared-device, shared-merchant and event-overlap heuristics. | Shared merchant counts lack an explicit time window. Pairwise overlap is quadratic in the fetched event count. Treat results as leads. |
| UI | `frontend/src/App.tsx`, `pages/SearchPage.tsx`, `components/GraphCanvas.tsx` and `features/hypergraph` implement routing, search and graph views. | Keep React Flow, TanStack Query and the legacy demo. New search keys must include dataset and query scope. |
| Security | `backend/build.gradle` has no Spring Security dependency. `GlobalExceptionHandler.java:42` returns exception text. Compose publishes app, Browser and Bolt ports with development defaults. | No authenticated ingestion, dataset isolation, redacted audit, retention or evidence export is implemented. Never load customer data into this baseline. |
| Domain | `DataSeeder.java` assumes bank products, merchants, card purchases, wires, USD and risk-labelled fixtures. | Make event types and provenance explicit. Scope uploaded identities independently of legacy IDs. Avoid floating-point exposure calculations. |
| Tests | Java service mocks, detector/scorer tests and a source-level Cypher test. Three frontend tests. | No Testcontainers behavioral suite, authorisation/isolation tests or automated browser E2E was found. CI's Compose smoke covers health, dashboard and a seeded hypergraph. |

## Runtime and dependencies

`backend/build.gradle` sets Java 21, Boot 3.5.16, dependency-management 1.1.7 and springdoc 2.10.1. CI uses Gradle 8.14.3 and Node 22. The frontend declares React 18, Router 7.18.2, React Flow 12.12.1, Query 5.108.4, Vite 5 and Vitest 2 (ranges unless locked).

`docker-compose.yml` selects Neo4j 5.24 Community. Neo4j documents Community as GPLv3. GDS is separately editioned and uses an in-memory projection. It is absent from Compose. Validate a maintained Community image and the precise server/driver combination in integration tests before deployment. Review redistribution obligations before packaging for third parties. [Neo4j editions](https://neo4j.com/pricing/) · [GDS editions](https://neo4j.com/docs/graph-data-science/current/introduction/)

All nine preferred ECC skill directories were found in the installed ECC cache. Directory presence is an inventory result.

## Local verification

- Docker Engine responded and `docker compose config --quiet` passed.
- `npm ci` installed from the lockfile and reported 11 advisories across the full dependency tree. Triage development tooling separately.
- `npm test` passed all three tests and `npm run build` succeeded.
- `npm audit --omit=dev` returned zero vulnerabilities after rerunning with registry access.
- `gradle :backend:test --no-daemon` passed inside the CI Gradle 8.14.3/JDK 21 container. Compilation reported deprecated API use in GraphQueryRepository.
- Compose runtime smoke and browser E2E were not run locally for this assessment. Configuration validation alone does not verify Neo4j runtime compatibility.

No production readiness or scale claim follows from these checks.
