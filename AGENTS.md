# Repository Guidelines

## Project Structure & Module Organization
Backend services live in `src/main/java/com/spring/aicodemother`, split by layer (`controller`, `service`, `mapper`, `model`, `utils`) to keep responsibilities clean. Configuration, i18n, and static assets sit in `src/main/resources`, while SQL bootstraps live under `sql/`. The Vue 3 frontend is in `ai-code-mother-frontend/src`, and mirrors component/layout folders. Tests shadow production code inside `src/test/java` so every package has a matching test home.

## Build, Test, and Development Commands
Use `./mvnw clean verify` for a full backend build with unit tests, or `./mvnw spring-boot:run` to launch the API locally with hot reload. Package a runnable JAR via `./mvnw -DskipTests package && java -jar target/*.jar`. Frontend contributors run `cd ai-code-mother-frontend && npm ci` once, then `npm run dev` for the Vite server, `npm run build` for production bundles, and `npm run lint` / `npm run type-check` to catch regressions early.

## Coding Style & Naming Conventions
Java code follows 4-space indentation, lowercase packages, PascalCase classes, camelCase members, and UPPER_SNAKE_CASE constants. Prefer constructor injection and keep controllers orchestration-only—business logic belongs in services. DTO/VO types guard external contracts. Vue and TypeScript use 2-space indentation, PascalCase components, and kebab-case filenames such as `app-chat-page.vue`; align with ESLint + Prettier before opening a PR.

## Testing Guidelines
JUnit 5 with Spring Boot Test drives backend coverage; name files `*Test.java` and mirror package paths. Mock integrations that reach external services. Run `./mvnw test` before pushing, and add focused tests whenever behavior changes. Frontend checks rely on `npm run lint` and `npm run type-check`; add Vitest suites if you touch complex UI logic.

## Commit & Pull Request Guidelines
Follow concise, imperative commit messages (Conventional Commits like `feat:` or `fix:` are encouraged). PRs should explain scope, link issues, and include UI screenshots when frontend changes apply. Never skip mentioning migrations, Prometheus dashboards, or rate-limit tuning if they change. Ensure CI-ready: backend build green, frontend linting clean, and new tests committed.

## Security & Configuration Tips
Seed databases with `sql/create_table.sql`, and keep secrets in environment variables or `application-*.yml`. Rate limiting uses Redisson—verify Redis connectivity in non-dev setups. Monitoring ties into `prometheus.yml` and `ai_model_grafana_config.json`; update dashboards whenever metrics names change.

## Agent-Specific Instructions
Scope changes tightly to requested tasks, avoid drive-by refactors, and document all adjustments in `todo.md`. When producing files or code, prefer incremental updates, note follow-up questions, and leave review notes summarizing risks for the next contributor.
