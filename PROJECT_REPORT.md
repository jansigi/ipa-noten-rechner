# Projektbericht

## CI/CD-Pipelines (Kurzbeschreibung)
- Backend-Pipeline: Linting mit ktlint, automatisierte Unit- und Integrationstests, Prüfung der Mindest-Testerfolgsquote (>= 80%), Build und anschliessendes Deployment in die Staging-Umgebung.
- Frontend-Pipeline: ESLint-Linting, automatisierte Unit-Tests mit JUnit-Report, Pruefung der Mindest-Testerfolgsquote (>= 80%), Build und anschliessendes Deployment in die Staging-Umgebung.
- Testergebnisse werden als JUnit-Reports in der GitHub Actions-Uebersicht angezeigt.
- Sensible Daten (z. B. Staging-API-Key) werden ueber GitHub Secrets bereitgestellt.
