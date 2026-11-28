# ADR 0003 | Versioned Identity and Evidence

Status: proposed. ADR 0001 remains historical context for the demo.

| Node | Required unique key | Other required properties |
| --- | --- | --- |
| Dataset | datasetId | ownerActorKey, lifecycleState, createdAt |
| DatasetVersion | datasetId, versionId | contractVersion, contentDigest, mappingDigest, state, createdAt |
| SourceNamespace | datasetId, namespaceKey | source designation, normalization version |
| Account | datasetId, namespaceKey, externalId | scope and stable source identity |
| Identifier | datasetId, namespaceKey, kind, normalizedValue | scope and typed normalization version |
| Event | datasetId, versionId, namespaceKey, externalEventId | observedAt UTC, eventType |
| SourceObservation | datasetId, versionId, namespaceKey, recordNumber, field | observedAt, source field, digest reference |

Scope Account and Identifier to the dataset and namespace; attach version-scoped typed relationships for participation. In templates that cannot guarantee stable account IDs, make the source namespace version-specific. Device, network and beneficiary values are identifier kinds in the first slice. Events anchor participation and observations, without a second hypergraph projection. Each edge carries datasetId and versionId; dataset-wide endpoints must match dataset and namespace, and versioned endpoints must additionally match version.

Create named single/composite property uniqueness constraints with backing indexes. Missing key properties must be rejected at the write boundary, since Community uniqueness does not provide Enterprise existence or node-key enforcement. Use the identifier composite index for full-key seeks and add named range indexes for version cleanup and event timestamps only where plans warrant. Verify schema and plans on the pinned image. [Neo4j 5 constraints](https://neo4j.com/docs/cypher-manual/5/constraints/create-constraints/)

Publish corrected inputs as immutable new versions; evidence pins version, event key and source record/field references until deletion. A row asserts observed participation only for that row, timestamp and field. Derived associations cite their source observations and rule version. Analyst conclusions have actor and time and remain separate from source assertions. Deduplicate canonical events while retaining distinct supporting observation references as needed. Never infer historical participation from an unrelated account edge or merge accounts from shared devices or network values.
