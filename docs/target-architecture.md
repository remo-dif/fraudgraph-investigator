# Target Architecture | Thin Synthetic MVP

Keep the Spring Boot and React monolith, Neo4j Community and Compose. The first milestone is an isolated synthetic import-to-delete journey. Reuse Commons CSV, DTO conventions, TanStack Query and React Flow. Keep the financial demo usable in its own profile and datastore.

## Decisions

- [ADR 0002](adr/0002-dataset-isolation.md) sets the physical boundary, profiles, authorization and cache scope.
- [ADR 0003](adr/0003-versioned-evidence.md) defines scoped identity, constraints and provenance.
- [ADR 0004](adr/0004-import-publication.md) defines atomic import, retries and deletion.
- [ADR 0005](adr/0005-bounded-evidence-queries.md) defines the first investigative reads and budgets.

Imported-data requests flow through authentication, scope policy and dedicated repositories to a private instance. Use an event-centric graph, with source observations separate from derived associations and human conclusions. A fixed CSV template feeds the first version. Search one typed identifier, show dated source evidence and export an allowlisted JSON result with dataset version, query/rule version, source references, ordering and truncation. Generate exports on demand within the authorization boundary. Already downloaded copies are outside revocation.

Bound upload admission before multipart parsing, derive per-actor and global concurrency limits from heap and spool budgets, and use quota-limited private temporary storage with abort cleanup. Sanitize internal errors, minimize response fields and logs, and audit actor, action and opaque scope IDs. The current preview is synthetic-only. See [threat model](threat-model.md) for pilot gates.

The [MVP status](mvp-status.md) describes what exists today. This document describes design decisions pending implementation.
