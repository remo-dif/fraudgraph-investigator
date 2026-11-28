# Threat Model

Assets include source records, pseudonymous identifiers, credentials, relationship graphs, analyst notes and exports. Trust boundaries are the browser, TLS ingress, authenticated API, CSV parser, database, export/download surface and backup store. Consider unauthenticated callers, authenticated analysts exceeding their scope, malicious uploaded content and operational mistakes.

| Risk | Proposed control | Required verification |
| --- | --- | --- |
| Unauthorised access or dataset mixing | Spring Security, deny-by-default API rules, dataset ownership checks on every lookup and export, scope all traversal endpoints. Keep Neo4j private with dedicated credentials. | Anonymous and wrong-dataset requests fail even with known IDs. Identical tokens in two datasets never link. Verify legacy demo cannot read imported data. |
| Parser and resource abuse | Stream bounded CSV, cap bytes/records/fields/concurrency, reject unsupported formats, use private temporary storage with cleanup. Use server-generated IDs and no uploaded execution. | Malformed UTF-8, quote failures, oversized content, interruption and duplicate concurrency. |
| Injection and information leaks | Parameterised Cypher, allowlisted labels and fields, generic error codes, no raw CSV values in logs. Escape dangerous spreadsheet cells including leading whitespace/control prefixes. | Injection-like values remain data, responses and logs are redacted, exports are inert when opened. |
| Traversal exhaustion and false association | Timeouts, degree caps, typed relationships and deterministic limits. Mark IP/device reuse as reviewable evidence rather than merging identities. | Shared NAT and family devices, hub load, bounded timeout and ambiguous identifiers. |
| Export misuse and data residue | Authorise and audit import/search/export/delete by actor and dataset. Use short-lived exports, retention jobs and encrypted backups. | Restore drill, export expiry, deletion retry and restoration without resurrecting deleted datasets. |

Deletion must cover graph nodes, source/preview artifacts, analysis snapshots and stored exports. Backup copies expire under a documented schedule. Apply deletion records on restore and document the limits of removing copies already downloaded. Audit records should retain minimal opaque IDs and operation metadata according to retention policy. Firewall, credentials, log policy, recovery tests and access controls are deployment gates for real customer data.
