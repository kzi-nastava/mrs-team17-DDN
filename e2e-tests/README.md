# E2E Tests

Standalone Selenium end-to-end tests live in this module.

## Prerequisites

- Backend running (default API URL used by frontend flow)
- Frontend running on `http://localhost:4200` (or set `-De2e.frontend.url=...`)
- PostgreSQL available for fixture setup (defaults: `jdbc:postgresql://localhost:5432/ddn`, user/pass `ddn`)
- Chrome and matching ChromeDriver available on PATH

## Run

```bash
./mvnw test -De2e.headless=true
```

Optional overrides:

```bash
./mvnw test \
  -De2e.frontend.url=http://localhost:4200 \
  -De2e.db.url=jdbc:postgresql://localhost:5432/ddn \
  -De2e.db.user=ddn \
  -De2e.db.password=ddn \
  -De2e.wait.seconds=15 \
  -De2e.headless=true
```
