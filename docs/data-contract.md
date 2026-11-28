# Canonical CSV Contract v1 (Proposed)

One row per event. Accept UTF-8 comma-separated CSV with a single header, optional BOM and standard CSV quoting, parsed as data. Require explicit source-to-canonical mapping. Reject duplicate headers or mapping targets. Ignore unmapped columns and never execute cell content. Proposed starting limits are 10 MiB, 50,000 data rows, 64 columns and 256 characters per mapped scalar. Enforce transport and streaming limits independently.

| Field | Validation and semantics |
| --- | --- |
| account_id, event_id | Required opaque, case-sensitive strings. Trim outer whitespace, preserve internal content and leading zeros. Reject controls and empty values. One event ID per dataset version. |
| event_type | Required documented vocabulary, e.g. ACTIVITY, REWARD, PAYOUT, TRANSACTION. Map customer-specific values explicitly. |
| timestamp | Required ISO 8601 with explicit offset. Store UTC instant and source row provenance. Reject local or ambiguous time. |
| campaign_id, device_id, beneficiary_id | Optional opaque identifiers, each with its own namespace. Blank means absent. |
| ip_token | Optional opaque, customer-issued token. Preserve case and treat as confidential pseudonymous data. Sharing is weak evidence. |
| reward_amount, payout_amount | Optional decimal strings with no exponent or separators. Reject excess scale, negative values and overflow. For reversal use status and a separate event linkage in a future contract if available. Never infer net loss. |
| currency | Required when either amount is present. Uppercase ISO 4217 code from a versioned allowlist with known minor-unit scale. Unknown or unsupported currency is a row error. No conversion. |
| status | Optional mapped status such as COMPLETED, PENDING, REVERSED or UNKNOWN. Missing means UNKNOWN and must be separately reported in exposure. |
| fraud_flag | Optional true or false. Blank is unknown. A positive flag is a customer assertion requiring its own provenance. It does not supply a confirmed monetary amount. |

Validate without writing for preview and return capped field/row/error codes without raw values. Commit requires revalidation and the same byte digest, mapping and contract version. Store dataset ID, version, digest, mapping version, source record number, importer and import time. Exact duplicate rows can be counted once, while conflicting duplicate event IDs reject the import. Reject malformed files atomically. Identical bytes plus mapping within the same authorised dataset scope should resolve to the existing import, including concurrent retries. Use a server-generated dataset ID and avoid leaking digest existence across access boundaries.

Current v1 fields lack a separate ledger ID, confirmed fraudulent amount and authoritative reversal link. Use event IDs for search, keep reversed and completed amounts separate, and report these limitations. Any extra fields require a versioned contract. JSON keeps original canonical values, while CSV export additionally escapes formula-like cells.

## Preview validator

The Java validator in `backend/src/main/java/com/fraudgraph/investigator/dataset/CsvPreviewValidator.java` accepts an input stream and a canonical-field-to-source-header map. It reads at most 10 MiB plus one byte into memory, validates at most 50,000 data records, and returns up to 25 sample records or 100 errors. Error positions are logical CSV record numbers with the header at record 1. Errors report the first failing field per record and omit source values. An invalid file returns no sample.

The initial currency allowlist is EUR, USD, GBP, JPY and KWD. Duplicate comparisons use normalized canonical fields only. Identical normalized events count once, while conflicting values for an event ID invalidate the preview. Source digests, persisted import idempotency, upload controls and deletion belong to later increments.
