# Technical Debt Report

## Critical Debt

- API endpoints are unauthenticated and unaudited.
- Unexpected backend errors can expose internal exception messages.
- Hypergraph modeling is new and should become the primary investigation abstraction for multi-entity events.

## High Debt

- `GraphQueryRepository` mixes query execution, mapping, dashboard metrics, alert detail, search, graph traversal, and detection rules.
- Seed data and constraints are embedded in startup code.
- Repository tests currently verify source strings rather than Neo4j behavior.
- Frontend is page-oriented rather than feature-oriented.

## Medium Debt

- Graph visualization contains layout decisions in rendering components.
- Detection rules are synchronous and request-time.
- CORS is local-development focused.
- Docker images are optimized for developer convenience, not production deployment.

## Low Debt

- Screenshot placeholders still need real captures.
- Documentation should be expanded with a developer onboarding guide and deployment notes.
- Keyboard shortcuts and deeper accessibility affordances are not yet implemented for graph workflows.

