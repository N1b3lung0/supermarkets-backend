# GitHub Repository Setup Guide

Manual configuration steps that cannot be automated via code — apply these once after pushing the repo to GitHub.

---

## Branch Protection Rules

Apply to both `main` and `develop` in **GitHub → Settings → Branches → Add branch ruleset**.

### `main` — production branch

```
Branch name pattern: main

✅ Restrict pushes that create matching refs
   └─ Restrict who can push: only repository owners

✅ Require a pull request before merging
   ├─ Required approvals: 1
   └─ Dismiss stale pull request approvals when new commits are pushed

✅ Require status checks to pass before merging
   ├─ Require branches to be up to date before merging
   └─ Required status checks:
        - Format check
        - Style check
        - Unit tests
        - Architecture tests
        - Integration tests

✅ Require conversation resolution before merging
✅ Do not allow bypassing the above settings
```

### `develop` — integration branch

Same rules as `main` except required approvals can be 0 for solo development.

---

## Required Secrets

Set in **GitHub → Settings → Secrets and variables → Actions**:

| Secret | Purpose | Required for |
|---|---|---|
| `GRADLE_CACHE_ENCRYPTION_KEY` | Encrypts Gradle build cache in GitHub Actions | CI + Release |

Generate the key locally:
```bash
openssl rand -base64 32
```

> All other secrets (`DB_*`, `JWT_*`, scrapers) are only needed for a cloud deployment — not for CI since tests use Testcontainers.

---

## Dependabot

Enable in **GitHub → Settings → Security → Code security → Dependabot**:

- ✅ Dependabot alerts
- ✅ Dependabot security updates
- ✅ Dependabot version updates (reads `.github/dependabot.yml` automatically once enabled)

---

## GHCR (GitHub Container Registry) — for releases

The release workflow pushes to `ghcr.io/{owner}/supermarkets`. No extra configuration needed — `GITHUB_TOKEN` has `packages: write` permission already declared in `release.yml`.

To pull the image locally after a release:
```bash
docker pull ghcr.io/{owner}/supermarkets:latest
```

---

## Environments (optional, for future deployment)

If you add cloud deployment later, create **GitHub → Settings → Environments**:

| Environment | Protection rules |
|---|---|
| `staging` | No approval required |
| `production` | Require 1 reviewer approval before deploy |

