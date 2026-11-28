# Repository Instructions

## Git workflow

- Keep `main` as the stable integration branch. Develop on short-lived branches and do not introduce a permanent `develop` branch.
- Start each phase branch from the latest `main`. Keep one primary pull request per implementation phase.
- Name roadmap branches `feature/phase-<number>-<description>`, using a lowercase phase identifier and kebab-case description.
- For work outside a roadmap phase, use `feature/`, `fix/`, `security/`, `refactor/`, `test/`, `docs/`, `chore/` or `hotfix/` according to the change.
- Use Conventional Commits. Include the phase in roadmap PR titles, for example `feat: phase 1A - dataset persistence foundation`.
- Require passing CI and security checks before merging. Require independent review for security-sensitive changes.
- Merge only with explicit approval. Never rewrite shared Git history.
- Delete merged feature branches once no active work depends on them. Preserve historical PR references, including PR #26.
- Update `docs/mvp-status.md` after each completed phase.
