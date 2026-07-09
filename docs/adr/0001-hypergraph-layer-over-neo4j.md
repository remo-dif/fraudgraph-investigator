# ADR 0001: Event-Centric Hypergraph Layer Over Neo4j

## Status

Accepted

## Context

Fraud investigations often need to reason about events involving many entities at once. A payment may involve a customer, account, card, device, IP address, merchant, and location. Modeling that as only pairwise edges makes the event harder to inspect and can hide suspicious clusters.

Neo4j remains a strong persistence layer for traversals, indexing, and graph query performance. The application should not expose Neo4j implementation details to the frontend.

## Decision

Add a backend hypergraph layer that represents meaningful fraud events as domain hyperedges.

Neo4j will persist hyperedges as event nodes:

```cypher
(:Customer)-[:PARTICIPATED_IN]->(:PaymentEvent)
(:Account)-[:PARTICIPATED_IN]->(:PaymentEvent)
(:Device)-[:PARTICIPATED_IN]->(:PaymentEvent)
(:Merchant)-[:PARTICIPATED_IN]->(:PaymentEvent)
(:IPAddress)-[:PARTICIPATED_IN]->(:PaymentEvent)
```

The backend maps these persistence details into frontend-neutral DTOs:

- `HypergraphDTO`
- `HyperNodeDTO`
- `HyperEdgeDTO`
- `SuspiciousHypergraphPatternDTO`

## Consequences

- The frontend can render event-centric investigations without knowing Cypher, labels, or Neo4j relationship names.
- Future persistence changes can be isolated to the repository/mapper layer.
- Detection rules can be expressed around event participation rather than only pairwise relationships.
- Additional mapping code is required, but it creates a stronger architecture boundary.

