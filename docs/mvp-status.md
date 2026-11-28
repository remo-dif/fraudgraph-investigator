# MVP Status

Source baseline: `feature/dataset-ingestion` at `75cfa70`. This is the authoritative implementation status for the investigative search milestone.

| Status | Capability |
| --- | --- |
| Implemented | Financial demo with Neo4j, cases, graph and hypergraph. Separate synthetic CSV preview API with a configured bearer credential, multipart size settings, normalized sample and structured validation errors. |
| Verified | At the source baseline, the backend suite passed in Gradle/JDK 21 including parser and MockMvc preview tests. Phase 0.3 rechecked Git source identity and Compose configuration only. Earlier frontend build/tests were on `2145aed`. |
| Planned | Profile and instance isolation, principal and owner checks, versioned Neo4j storage, atomic publication and revocation, exact search, dated evidence, JSON export and synthetic E2E. See the [roadmap](mvp-roadmap.md). |
| Deferred | Advanced entity resolution, fuzzy matching, fraud scoring, community detection, exposure totals, mapping UI, connectors, GDS, PDF, multi-tenant SaaS, billing and cross-customer matching. |
| Blocked | Real customer intake remains blocked pending verified pilot ingress, secret handling, authorization, resource budgets, audit, retention, deletion and backup restore. Phase 1A implementation awaits review approval. |
