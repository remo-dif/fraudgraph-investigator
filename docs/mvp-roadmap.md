# MVP Roadmap

Phase 0 produces this baseline, proposed contract, target design and release gates. Each later phase should land in small reviewable PRs with ADRs for accepted decisions.

| Phase and scope | Dependencies and affected components | Acceptance and required tests | Security implications, risks and decisions |
| --- | --- | --- | --- |
| 1. Ingestion | Contract v1, Java CSV parser selection, `backend/.../dataset`, schema migrations, `frontend/src/features/datasets`, Compose profiles | Preview validates small synthetic CSV with explicit mapping and stable errors. Commit writes atomic provenance and idempotency records. Delete removes scoped records safely. Test parser limits, time/currency, duplicates, restart and deletion with Neo4j Testcontainers. | Introduce authentication and operation/ownership checks before enabling persistence. Decide parser, profile, limits and import transaction sizing. |
| 2. Resolution and search | Scoped schema, `resolution`, `search`, existing graph DTO/React Flow components, TanStack Query | Search exact typed IDs with bounded paths, source rows, times and matching reasons. Test Cypher behavior, same IDs across datasets, legitimate shared infrastructure, hub bounds and authorisation. | Cap expansion and avoid weak-signal merges. Decide identifier normalization and ranking weights by type. |
| 3. Clusters and exposure | Validated events and resolution, `cluster`, workspace timeline and exposure panel | Multi-identifier, beneficiary, coordinated participation and time-window rules reproduce expected clusters. Test synthetic precision/recall, duplicate events, mixed currencies, status buckets and overflow. | False positives and double counting are key risks. Decide window defaults, holdout fixtures and exposure taxonomy. |
| 4. Evidence | Versioned graph results, `evidence`, investigation detail and export UI | An analyst saves notes and exports CSV/JSON with stable investigation ID, dataset version, source references, paths, rules, strength, timestamp, exposure and limitations. Test replay, CSV injection and access checks. | Decide snapshot lifetime and export storage policy. Minimise copied sensitive values. |
| 5. Hardening and benchmarks | Integrated workflow, Compose and CI, Testcontainers and Playwright | Exercise import-to-delete E2E, permission and isolation failures, retention/restore, redaction and resource limits. Record throughput, disk, memory, search/traversal latency and cluster duration. | Verify all real-data gates. Decide retained metrics, supported Neo4j image and production resource budget. |
| 6. Demo and deployment | Verified MVP, `README.md`, Compose production profile and synthetic fixture generator | Document setup, private VPS operation and rollback. A fresh Compose install completes the synthetic workflow and verifies expected paths and exports. | Separate demo credentials/data. Decide operational owner and retention schedule before real deployments. |

## Benchmark protocol

Use seeded fixtures with 1k, 10k and 50k events including legitimate shared devices, NAT and labelled synthetic clusters. Record commit, hardware, image versions, JVM and heap settings, row mix and cold/warm state. Capture import rows/sec, Neo4j volume delta, container peak memory, p50/p95 search and traversal over repeated fixed queries, and cluster wall time. Report synthetic ground-truth error rates and retain the generator seed. No benchmark results were collected in Phase 0.

## Smallest implementation increment

Start Phase 1 with a pure Java parser/validator and a checked-in synthetic fixture. Accept explicit mapping and return a bounded preview with normalized rows and row-indexed error codes. Test valid optional blanks, malformed CSV, byte and row caps, duplicate conflicts and timestamp/currency parsing. Keep the increment callable from tests without an HTTP endpoint or database writes. Add the authenticated preview endpoint and persistence only in later increments after review.
