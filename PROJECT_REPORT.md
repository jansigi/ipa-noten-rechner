# Projektbericht

## CI/CD-Pipelines (Kurzbeschreibung)

### Überblick
- Workflow `Backend CI`: `ktlint` → Tests → Docker Build/Push.
- Workflow `Frontend CI`: ESLint → Playwright E2E Tests → Docker Build/Push.
- Workflow `Database CI`: DB-Start/Readiness-Check → Docker Build/Push.
- Workflow `Staging Deploy`: End-to-End "Deploy" via `deployment/docker-compose.yml` inkl. Smoke-Test.

### Qualität-Gates (DevOps / Modul 324)
- **Pipeline stoppt bei Fehlern:** Lint- oder Test-Fehler brechen den Workflow mit `::error::...` ab.
- **Testergebnisse sichtbar:** JUnit-Reports werden in GitHub Actions via `dorny/test-reporter` angezeigt.
- **80%-Regel (Pass-Rate):** In den Workflows wird eine minimale Erfolgsquote von >= 80% der Testfälle geprüft.

### Secrets
- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`: Push der Staging-Images nach Docker Hub.
- `POSTGRES_PASSWORD`: Wird beim Staging-Stack (docker compose) verwendet.

### Branch-Policy (Versionskontrolle)
- Für die Bewertung wird empfohlen, in GitHub Branch Protection zu aktivieren:
  - Required status checks: `Backend CI`, `Frontend CI`, `Database CI`
  - Kein Merge bei fehlschlagenden Checks

## Testkonzept (Modul 450)

### Testarten und Teststufen
- **Backend Unit-Tests:** z.B. Berechnungslogik (Grading) isoliert testen.
- **Backend Integration-Tests:** z.B. Repository-/Route-Tests mit Test-DB.
- **Frontend E2E-Tests (Playwright):** User-Flows im Browser (mit gemockten Backend-Responses), inkl. JUnit-Report.

### Abdeckung der User-Stories (Traceability)
- Person erfassen → `frontend/tests/personen.spec.ts`
- Kriterien anzeigen/filtern → `frontend/tests/criteria.spec.ts`
- Anforderungen abhaken + Notiz speichern → `frontend/tests/checklist.spec.ts`
- Noten-/Stufenlogik (Backend) → `backend/src/test/kotlin/ch/jf/ipa/service/GradingServiceTest.kt`

### Testprotokolle / Ergebnisse
- Lokal: JUnit-XML im jeweiligen `build/test-results/...` bzw. `frontend/test-results/junit.xml`.
- CI: Anzeige der Tests in der GitHub Actions Oberfläche (Test Reporter).
