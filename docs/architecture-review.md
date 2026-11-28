# FraudGraph Investigator — independent architecture and security review

Reviewed 28 November 2026 against `feature/dataset-ingestion` at `75cfa70ea6ee32c4abd8a54154f3a80731172af0`. The target architecture, CSV contract, MVP roadmap, threat model and product vision are proposals. The ADR, architecture assessment, technical-debt report, PR roadmap, testing strategy and current-state assessment provide earlier context. The latter explicitly describes `2145aed`; the reviewed branch adds an authenticated synthetic preview with no graph writes.

Severity describes impact in the intended deployment. **Critical** means an evidenced immediate compromise of a core boundary for the in-scope data. **High** blocks the affected release or implementation decision. **Medium** requires a targeted correction before the affected capability is accepted. **Low** affects clarity or maintenance. No Critical finding was established on the synthetic repository baseline. No live customer deployment or incident was assessed.

Java paths below are relative to `backend/src/main/java/com/fraudgraph/investigator/` unless otherwise stated.

## Verified findings

Verified means evidenced by checked-in source or an explicit gap in a proposal. Runtime outcomes and proposed acceptance tests are separate.

### V1 · High — The dataset boundary is a release gate

- **Evidence.** `dataset/PreviewSecurity.java:26–57` grants a shared operator authority for preview and permits all requests in the fallback chain. `controller/CaseController.java:26–35` exposes mutations under that chain. `repository/GraphQueryRepository.java:134–177` and `hypergraph/HypergraphRepository.java:23–58` run unscoped reads. `config/DataSeeder.java:13–30` registers a startup runner without a demo profile. `docs/target-architecture.md:3,20` specifies separation as a target.
- **Risk.** In an imported-data deployment, the reused demo routes or credentials could bypass a scoped ingestion API. In a shared graph, authorizing the starting node alone leaves intermediate traversal nodes, relationships, and derived records outside the guarantee. Dataset scoping also does not give actor-level authorization.
- **Correction.** Run the demo with its own datastore, credentials and route registration. For the first customer pilot, prefer a private single-customer deployment with dataset ownership checks. Derive actor and dataset/version access server-side through a shared policy and require that scope at every repository entry point, including aggregation, case links, export and delete. Include principal and dataset/version in UI cache identity and clear prior data on authority changes.
- **Acceptance.** Using two actors and two datasets with overlapping IDs and tokens, verify allowed access and deny anonymous or wrong-dataset reads, mutations and existence disclosure. Reject cross-scope relationships on write and exclude them on read. Prove that demo routes, seeder and credentials cannot reach an imported datastore. Verify UI account or dataset switching cannot display prior scoped results.

### V2 · High — Local Compose defaults cannot serve as a pilot deployment

- **Evidence.** `docker-compose.yml:2–37` publishes Neo4j HTTP/Bolt and the backend, and supplies a checked-in development password fallback used by the API. `backend/src/main/resources/application.yml:8–12` supplies local connection defaults. `.github/workflows/deploy.yml:22–34` is a placeholder. `docs/threat-model.md:7–13` calls for private Neo4j and deployment controls.
- **Risk.** Reusing local defaults on a reachable host can expose the database and bypass API controls. The shared bootstrap/runtime identity also increases the consequences of credential exposure. Actual hosting and reachability remain unknown.
- **Correction.** Make demo defaults explicitly local and bind them to loopback. For the pilot, expose only TLS ingress, make Neo4j and backend private, require non-default deployment credentials and separate bootstrap from runtime privileges where the supported edition permits. Record any edition limits and use instance isolation where privilege separation is insufficient.
- **Acceptance.** Validate the production Compose configuration and run a synthetic deployment check confirming only intended HTTPS ingress is reachable. Startup fails with missing or default secrets. Document and verify actual runtime privileges against the selected edition.

### V3 · High — Event identity and versioning are underspecified

- **Evidence.** `docs/target-architecture.md:20–26` proposes dataset plus typed external ID and immutable evidence references. `docs/data-contract.md:5–19` allows an event ID once per dataset version. `config/DataSeeder.java:51–56` creates id-only demo constraints. `docs/adr/0001-hypergraph-layer-over-neo4j.md` models event participation but predates the import contract.
- **Risk.** Carrying over global IDs or using the proposed dataset key for versioned events can collapse imports, overwrite corrected observations, or invalidate replay. A uniqueness constraint alone cannot require properties to exist or keep edge endpoints in the same scope.
- **Correction.** Retain Neo4j and one canonical Event per scoped source event. Use a Dataset/Version boundary, a scoped account identity where the source guarantees stability, and versioned event/observation identity with explicit source namespaces. Use typed role edges for observed participation. For the MVP, model device, network and beneficiary as typed identifiers unless they need independent lifecycle or attributes. Add a separate ledger node only when the source supplies a distinct ledger identity. Specify immutable revision behavior and mandatory scope validation in one ADR. Neo4j 5 composite property uniqueness differs from Enterprise key constraints. [Neo4j 5 constraint documentation](https://neo4j.com/docs/cypher-manual/5/constraints/create-constraints/)
- **Acceptance.** Apply the schema to a clean instance of the supported Community version. Duplicate full keys cannot be written, missing key components are rejected by the write boundary, equal external IDs in different namespaces/datasets/versions remain separate, and corrections preserve the older version's evidence until deletion. Query plans use the intended scoped lookup index.

### V4 · High — Demo graph edges can misstate historical evidence

- **Evidence.** `config/DataSeeder.java:266–286` attaches payment participants from account-level device/IP and card links. Lines 301–311 copy participants into derived alert events. `hypergraph/HypergraphRepository.java:23–58` reads generic participation edges. `docs/target-architecture.md:21–22` distinguishes observed links and inferred associations, but leaves rules for source authority and signal independence open.
- **Risk.** A historical device can appear to have made a payment. Shared NAT, shared households, recycled devices and correlated device/IP observations can generate misleading clusters. A derived alert can reinforce or double-count its own source evidence.
- **Correction.** Use imported row-specific observations and retain source row, timestamp, field, type and namespace. Render observed participation, inferred association, and analyst conclusion distinctly. Never merge accounts based on shared identifiers. Require time proximity, source-specific normalization, ambiguity and high-degree suppression for candidate links. Count duplicate observations and derived evidence once.
- **Acceptance.** Labelled fixtures cover benign shared NAT/devices, reused tokens across time or source namespaces, absent event-specific metadata, and duplicated alerts. Each rendered reason traces to the relevant row and time. Weak or correlated evidence remains distinguishable, and fixtures assert no identity merge or false assertion of event participation.

### V5 · High — Query response limits leave database work unbounded

- **Evidence.** `repository/GraphQueryRepository.java:132–177` uses an unlabeled search and all-relationship, bidirectional expansion with clamped depth and a late path cap. Lines 295–302 use another broad expansion. `hypergraph/HypergraphRepository.java:23–58` collects and sorts participants before limiting events. The inspected read paths bind request values; the formatted neighborhood depth comes from a clamped integer.
- **Risk.** Hubs, large intermediate aggregations and concurrent searches can consume database and API resources even when responses are small. The generic DTO property mapping in `repository/GraphQueryRepository.java:411–425` can expose newly added properties. Static review of these paths did not establish Cypher injection.
- **Correction.** Start from a labeled, indexed scoped identity. Use fixed parameterized templates, typed/directed hops, scope and time filters during expansion, per-hop and per-event fan-out limits, deterministic ordering and allowlisted projections. Set database/transaction deadlines plus endpoint concurrency limits and expose truncation. [Neo4j 5 query tuning guidance](https://neo4j.com/docs/cypher-manual/5/planning-and-tuning/query-tuning/)
- **Acceptance.** On the pinned Community image, retain query plans for common and high-degree synthetic cases. Show scoped index seeks, early pruning and bounded intermediate work. Record p95, database hits and memory under a documented concurrent workload. Verify controlled deadline and truncation responses. Untrusted values remain parameters and confidential sentinel properties never appear in API or export projections.

### V6 · High — Import publication and deletion need a lifecycle before persistence

- **Evidence.** `dataset/DatasetPreviewController.java:30–42` calls only the validator. `docs/data-contract.md:17–19` requires same-digest revalidation, atomic import and concurrent idempotency. `docs/mvp-roadmap.md:7,11` leaves transaction sizing and retention verification open. `docs/threat-model.md:13` requires deletion to cover derived data, artifacts and restored backups.
- **Risk.** A large single transaction may exceed resource budgets. Visible batch writes could leave a partially readable graph after failure or race with retry/delete. A digest alone provides neither authorization nor a guarantee of identical parsing semantics.
- **Correction.** Specify a small atomic transaction for the measured first limit, or use private staged batches and one atomic publication if measurement requires it. Bind dataset/version, bytes, mapping, contract and authorization to the import attempt. Define one owner for idempotency and clear states for visibility, retry, cleanup and deletion. Audit operations with actor, scope and safe metadata. Apply deletion records during restore.
- **Acceptance.** Concurrent equivalent imports yield one published version in the authorized scope. A wrong scope learns nothing about duplicate existence. Invalid input, interruption and concurrent delete leave no partial visible graph or orphaned artifacts. Delete revokes reads/exports, is idempotent and survives restore within documented backup retention.

### V7 · Medium — Preview has per-request limits but lacks an aggregate resource and privacy budget

- **Evidence.** `dataset/CsvPreviewValidator.java:26–31,62–105,195–237` buffers bytes and decoded text and retains normalized events for deduplication. `backend/src/main/resources/application.yml:2–5` caps multipart size. `dataset/DatasetPreviewController.java:36–40` sets no-store, but successful samples contain normalized identifiers and amounts. `exception/GlobalExceptionHandler.java:48–55` returns generic exception messages verbatim in response details.
- **Risk.** Parallel uploads can amplify heap or temporary disk use. Successful preview responses may carry sensitive identifiers and values. Unexpected errors can disclose internal or input details.
- **Correction.** Preserve Commons CSV and the existing structured validation. Apply ingress and application concurrency/rate budgets, private quota-bound temporary storage and cleanup. Stream and bound field allocation where needed, or lower the configured limit based on measurement. Define which preview fields are necessary, redact the remainder, and return correlation IDs with sanitized error codes.
- **Acceptance.** Real HTTP multipart tests verify configured limits, response redaction and cleanup for accepted/rejected uploads and interruptions. A bounded concurrent synthetic workload stays within documented heap and disk budgets. Unexpected exceptions produce stable responses and no sensitive content in client errors or logs.

### V8 · High — Existing CI cannot verify persistence or isolation

- **Evidence.** `backend/build.gradle` has no Testcontainers dependency. `backend/src/test/java/com/fraudgraph/investigator/repository/GraphQueryRepositoryCypherTest.java:10–17` checks source strings. Preview tests under `backend/src/test/java/com/fraudgraph/investigator/dataset/` exercise parser and MockMvc behavior. `.github/workflows/ci.yml` runs backend tests and a real Compose smoke covering health, dashboard and seeded case hypergraph. `docs/testing.md:3–12` recommends Neo4j Testcontainers, and `docs/mvp-roadmap.md:7–11` proposes behavioral tests.
- **Risk.** Smoke checks provide boot and route confidence, while mocks and source strings cannot establish Cypher semantics, constraints, write visibility, transaction rollback, or actor/dataset isolation. MockMvc multipart tests do not establish production transport limits.
- **Correction.** Make a Neo4j Community Testcontainers suite a required gate with the first persistence PR. Use the real migrations, driver and repository wiring. Keep fast parser and authorization tests and add embedded-server multipart coverage. Add one synthetic import-to-delete journey alongside the existing demo smoke.
- **Acceptance.** CI fails if the database-backed tests are skipped or fail. Assert expected graph values and provenance, scoped key/index behavior, cross-scope denial, idempotency, rollback/atomic publication, bounded traversal and deletion. The end-to-end check asserts fixture-derived response data and absence after delete, including export access where supported.

### V9 · Medium — Scope and reuse need a thinner first milestone

- **Evidence.** `docs/target-architecture.md:18–24` keeps one deployable but adds resolution, clusters, exposure and evidence. `docs/mvp-roadmap.md:7–12` leaves the end-to-end workflow to late phases. `docs/pr-roadmap.md:63–113` describes broad refactors and places security and verification late. Existing reusable seams include `dataset/CsvPreviewValidator.java`, Spring MVC and `Neo4jClient`, projected DTOs, `frontend/src/components/GraphCanvas.tsx`, `frontend/src/features/hypergraph/components/HypergraphCanvas.tsx` and TanStack Query hooks.
- **Risk.** The operations and analysis features, more than the runtime topology, make the proposed first release too broad for a solo developer to validate quickly. Reusing whole demo repositories or a second event projection would copy legacy assumptions rather than shorten delivery.
- **Correction.** Keep the Java/React/Neo4j monolith. Reuse the parser, canvases, API conventions and DTO projection approach. Add one scoped query path with its own tests. Deliver one template, one isolated import, one typed exact lookup and a dated evidence path, a minimal reproducible evidence view or single-format export, and delete. Establish required security and DB checks in that slice. Defer broad package refactors, generalized mapping UI/connectors, multi-rule clusters and scoring, GDS, rich notes and snapshot/export workflows, and multi-tenant SaaS until analyst feedback warrants them.
- **Acceptance.** One canonical roadmap supersedes conflicting sequencing. On a clean Compose install, show the thin slice end-to-end using a synthetic fixture. Every included feature supports that journey directly, and any deferred feature has a pilot signal that would justify reopening it.

### V10 · Medium — Exposure needs business semantics before aggregation

- **Evidence.** `docs/data-contract.md:9–19` permits separate reward and payout amounts and status but has no authoritative ledger or reversal linkage, or event-type/amount compatibility matrix. `docs/target-architecture.md:24` proposes per-event, per-currency aggregation.
- **Risk.** Unique event IDs and integer arithmetic still allow counting two representations of the same economic activity or presenting an estimate as loss.
- **Correction.** Defer exposure totals until a representative dataset establishes amount lineage. Define allowed event/amount combinations and separate measures by type, currency and status. Keep estimates and customer fraud assertions separate from confirmed loss. Avoid netting without authoritative links.
- **Acceptance.** A signed-off fixture truth table covers duplicate economic events, join fan-out, partial or unknown status, reversals, mixed currencies, overflow and missing amounts. Each reported subtotal has reproducible event lineage and clear exclusions.

### V11 · Low — Documents need an explicit status hierarchy

- **Evidence.** `docs/current-state-assessment.md:3,12` is pinned to the pre-preview baseline. `docs/testing.md:25–32` describes the demo workflow. `docs/mvp-roadmap.md:20` describes the preview API as a later increment, while `docs/data-contract.md:21–27` documents the implemented preview.
- **Risk.** A new contributor can mistake historical assessments for current controls or a seeded demo smoke for the proposed customer workflow.
- **Correction.** Preserve the dated baseline and label historical docs as such. Link one canonical MVP status and clearly distinguish the demo, synthetic preview and imported-data release gates.
- **Acceptance.** The entry point names the current branch capabilities, the owning architecture/roadmap documents and the exact suites for demo and imported data.

## Assumptions and decision conditions

| ID and severity | Evidence and assumption | Risk | Correction and acceptance criteria |
| --- | --- | --- | --- |
| A1 · Medium | `docs/product-vision.md:3–9` names market hypotheses. Analyst demand, representative source availability and identifiers' discriminative value are unverified. | A polished graph can produce technically reproducible but commercially low-value leads. | Agree on one analyst persona, one task and one permitted data template. Use benign and positive labelled fixtures, then validate with a pilot analyst. Reopen scoring, connectors or richer evidence only after a repeatable workflow need and acceptable false-positive cost are demonstrated. |
| A2 · Medium | `docs/mvp-roadmap.md:14–16` specifies 1k/10k/50k fixtures and records no benchmark results. Solo feasibility is a scope judgment, with no measured capacity or delivery estimate. | Unsupported volume expectations could force redesign or increase operational load. | Treat the thin synthetic slice as feasible in principle. Publish results on the intended host/image with realistic skew, concurrent imports/reads and declared memory/latency limits. Set a supported cap from the results and document a restore drill before a customer pilot. |

## Review execution

This is a static review. ECC architect, planner, security-reviewer and code-reviewer roles were applied via delegated read-only reviews. The seven installed ECC skills were used as review guidance. Search-first drove the in-repository reuse inventory and primary-documentation lookup. Spring patterns, security and Java standards informed the boundary review. TDD and verification informed the behavioral test and release criteria. No application tests, scans, startup checks or database benchmarks were executed for this review. Historical test claims in `docs/current-state-assessment.md` apply to its baseline only.

## Recommendation

**Approve with changes.** Retain the modular monolith and the event-centric Neo4j model for the thin synthetic MVP. Resolve the dataset boundary, deployment configuration, versioned identity, event provenance, bounded queries, import lifecycle and database-backed verification before permitting customer data. Validate one analyst workflow before funding the deferred features.
