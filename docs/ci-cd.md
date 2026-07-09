# CI/CD

FraudGraph Investigator uses GitHub Actions for validation, image publishing, and deployment handoff.

## Workflows

### CI

File: `.github/workflows/ci.yml`

Runs on pull requests, pushes to `main`, and manual dispatch.

Jobs:

- `frontend`: installs with `npm ci`, runs Vitest, builds Vite, and audits production dependencies.
- `backend`: runs `gradle :backend:test` inside the `gradle:8.10.2-jdk21` container.
- `compose`: validates `docker compose config`, builds all images, starts Neo4j/backend/frontend, waits for API health, and smoke-tests dashboard plus hypergraph endpoints.

The backend job intentionally uses containerized Gradle because the repository does not currently include a Gradle wrapper.

### Publish Images

File: `.github/workflows/docker-publish.yml`

Runs on pushes to `main`, version tags such as `v1.0.0`, and manual dispatch.

Publishes:

- `ghcr.io/<owner>/<repo>/backend`
- `ghcr.io/<owner>/<repo>/frontend`

Tags include branch, Git tag, and commit SHA.

Image names are normalized to lowercase before publishing because GHCR follows Docker image naming rules.

### Security

File: `.github/workflows/security.yml`

Runs on pull requests, pushes to `main`, and manual dispatch.

Checks:

- Trivy filesystem scan for high and critical vulnerabilities.
- The scan is intentionally run as a normal Docker command instead of SARIF upload so it works in private repositories without GitHub Advanced Security.

### Deploy

File: `.github/workflows/deploy.yml`

Manual workflow that documents the deployment contract. It is ready to be wired to the final hosting target once selected.

Inputs:

- `environment`: `staging` or `production`
- `image_tag`: container tag to deploy

## Required Repository Settings

- GitHub Actions enabled.
- Package write permission for the default `GITHUB_TOKEN`.
- Environments named `staging` and `production` if protection rules are desired.

## Recommended Next Steps

- Add Playwright e2e tests and run them after the Compose smoke test.
- Add container image scanning after image build.
- Enable GitHub Advanced Security later if SARIF upload and dependency-review gating are desired.
- Add deployment platform integration once hosting is chosen.
- Add branch protection requiring `CI` to pass before merge.
