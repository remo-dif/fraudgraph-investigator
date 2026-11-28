# ADR 0004 | Import Publication and Deletion

Status: proposed.

Validate the fixed template without graph writes. Commit revalidates exact bytes against the same mapping and contract, with a server-derived owner and dataset scope. Use a unique attempt key composed of datasetId, source namespace, SHA-256 bytes digest, mapping digest and contract version. Record requester and safe timestamps, and authorize before looking up an attempt. Never expose digest existence across scopes.

Attempt states are VALIDATED, WRITING, PUBLISHED and FAILED. Validation and writing are transient in the first implementation. Publish the attempt key, version and scoped graph atomically in one transaction; only PUBLISHED versions are readable. Failure rolls back all graph writes and records safe failure metadata separately. An equivalent retry returns the published version or retries after rollback. Serialize publication and deletion with an explicit write lock on the dataset lifecycle node and use the uniqueness constraint as a second guard. Retry only bounded transient failures and recheck authorization and lifecycle after lock acquisition.

Deletion transitions ACTIVE to DELETING under the same lock, revoking import/read/export. Reads and on-demand exports revalidate access before release; in-flight work started before revocation is drained or cancelled before deletion is acknowledged. Delete versioned nodes, observations and owned identifiers, then clean temporary artifacts. Repeated delete resumes cleanup. Retain a minimal tombstone to prevent resurrection and apply it on restore; transition to DELETED after cleanup. Document backup expiry and download limitations.

Start with a small synthetic fixture and one transaction. Measure heap, transaction duration, rollback, spool, concurrent requests and skew before choosing a supported cap. Parser guards are not safe persistence limits. Introduce private staging and atomic publication only if measured needs exceed the transaction budget.
