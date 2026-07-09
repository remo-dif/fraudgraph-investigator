# Security Notes

FraudGraph Investigator is currently configured for local development. Before production use, the project needs a formal security baseline.

## Required Controls

- Authentication through JWT, OIDC, or a demo analyst identity provider.
- Authorization for case creation, case status updates, and detection runs.
- Audit logging for analyst actions.
- Safe error responses that avoid leaking implementation details.
- Environment-specific CORS configuration.
- Input validation for search, graph expansion, case mutation, and filter parameters.

## Suggested Roles

- `ANALYST`: read alerts, inspect graphs, create cases.
- `SENIOR_ANALYST`: update case status, escalate cases, run detections.
- `ADMIN`: manage configuration and seed/demo controls.

## API Hardening Priorities

1. Protect mutating endpoints.
2. Add request correlation IDs.
3. Remove raw exception messages from 500 responses.
4. Add structured audit events for case transitions.

