# MVP Status

Phase 1A adds the separate import profile and Compose stack, named Community constraints and internal owner-scoped dataset/version/event writes and reads. The demo remains the default profile. The synthetic preview remains demo-only and has no database write path. See [testing](testing.md) for the mandatory database gate.

Phase 1B still owns CSV publication, request budgets, deletion and revocation. Customer data remains blocked. Production identity provider, runtime credential privileges, capacity budgets, retention and recovery remain open.

Source baseline: `feature/dataset-ingestion` at `75cfa70`. This is the authoritative implementation status for the investigative search milestone.

| Status | Capability |
| --- | --- |
| Implemented | Financial demo with Neo4j, cases, graph and hypergraph. Separate synthetic CSV preview API with a configured bearer credential, multipart size settings, normalized sample and structured validation errors. |
| Verified | On the Phase 1A working tree, the backend fast suite and both mandatory Testcontainers tests passed locally with Gradle 8.14.3 and Java 21. Both Compose configurations validated. The full demo Compose CI smoke and hosted checks have not run on this working tree. Earlier frontend build/tests were on `2145aed`. |
| Planned | Profile and instance isolation, principal and owner checks, versioned Neo4j storage, atomic publication and revocation, exact search, dated evidence, JSON export and synthetic E2E. See the [roadmap](mvp-roadmap.md). |
| Deferred | Advanced entity resolution, fuzzy matching, fraud scoring, community detection, exposure totals, mapping UI, connectors, GDS, PDF, multi-tenant SaaS, billing and cross-customer matching. |
| Blocked | Real customer intake remains blocked pending verified pilot ingress, secret handling, authorization, resource budgets, audit, retention, deletion and backup restore. Phase 1A implementation awaits review approval. |
