# ADR 0005 | Bounded Investigative Queries

Status: proposed.

First query accepts one device identifier. Normalize under its source policy, authorize an active published version and use a labeled Identifier seek with all composite key properties bound as parameters. Traverse only inbound OBSERVED_IDENTIFIER edges from scoped Events, then outbound typed actor/observation roles. Keep fixed direction and depth. Accounts sharing the device appear as observed on separate events, with no identity merge. Verify dataset, version and namespace at every hop before projection.

Use fixed query templates and early per-hop cap-plus-one to detect truncation. Maintain version-specific degree counts atomically and return an explicit hub suppression marker when degree exceeds a measured scan budget. Sort only bounded candidate sets; order by observed time and stable event key, then role and observation key. Allowlist identifiers, dated evidence and provenance fields; never serialize raw Neo4j nodes or property maps. Include truncation reasons and effective bounds in results and exports.

Set a driver transaction timeout plus API deadline and bounded concurrent queries per actor and globally. Reject overload with a controlled response and release permits on interruption. Use scoped index plans, legitimate shared infrastructure and hubs above the degree budget to verify early pruning, deadline behavior, deterministic order and absence of foreign-scope or confidential sentinel fields. Collect database hits, latency and memory under documented concurrency before enabling larger data.
