# MVP Roadmap

The first milestone is one synthetic dataset, predefined CSV validation, isolated import, one typed exact search, bounded related events and entities, dated source evidence, minimal reproducible JSON export and scoped delete/revocation. Preserve the demo independently. See [status and deferrals](mvp-status.md).

| Increment | Scope | Gate |
| --- | --- | --- |
| 0.3 | Reconcile independent review and freeze the first workflow. | Approve the decisions and Phase 1A scope. |
| 1A | Dataset boundary and persistence foundation. | Profiles and real Community integration tests prove ownership, keys and scoped storage. |
| 1B | Fixed-template validation and atomic publication, retries, revocation and cleanup. | Concurrent idempotency, rollback and deletion tests. Real HTTP limits and measured synthetic import cap. |
| 1C | Typed device lookup, bounded evidence, analyst UI and minimal JSON. | Query plans under skew and synthetic import-to-delete E2E. Demo smoke stays green. |
| Pilot gate | Private deployment, verified secrets and actor provider, recovery and analyst evaluation. | Actual resource and restore results, secure ingress and independent access checks before customer data. |

## Phase 1A | Dataset Boundary and Persistence Foundation

Objective: boot the import profile against its own Community instance and prove a minimal owned dataset/version graph write and scoped read through internal test fixtures. Example changed components are `application-demo.yml`, `application-import.yml`, profile registration for legacy controllers/seeder/security, `dataset/access`, `dataset/persistence`, versioned Cypher schema resources, backend Gradle integration test task and CI. Preserve the synthetic preview and existing demo smoke.

Initialize the named Dataset, DatasetVersion and Event uniqueness constraints first. Add SourceNamespace, Account, Identifier and SourceObservation constraints with the first writes to those labels, as specified in ADR 0003. Keep write entry points internal and validate every key component and scope before parameter binding. Use restrictive import security, separate credentials and server-derived identity. The mandatory real database gate checks migration, key uniqueness, missing-key rejection, actor denial, overlapping IDs, wrong-scope edges, and rollback. Verify profile registration and fail-closed configuration independently.

Deliverable acceptance requires the isolated profile to boot, migrations to be rerunnable, the demo to keep its existing behavior and CI to fail if persistence verification is unavailable. No upload-to-database endpoint, new UI, full publication state machine, search, export, deletion API or VPS changes in this PR.

Decisions to close in the PR: exact maintained Community image and driver combination, schema bootstrap mechanism, test identity wiring and enforcement of separate configuration. Community credential capabilities and cross-profile wiring need explicit negative tests. The smallest change is one internal repository transaction creating an owned dataset, version and event with integration tests against the dedicated instance.

## Measurement

Use seeded fixtures with increasing sizes and realistic skew, plus legitimate device and NAT reuse. Report host/image/heap, fixture seed, concurrency, import rows/sec, disk growth and peak heap/spool, rollback time, p50/p95 search and database hits. Determine limits from measured results. No benchmark result exists yet. Exposure, generalized resolution, clustering and advanced reports are outside this milestone.
