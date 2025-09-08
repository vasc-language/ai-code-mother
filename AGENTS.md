# Repository Guidelines

## Project Structure & Module Organization
- Backend (Spring Boot, Maven): `src/main/java/com/spring/aicodemother` organized by layer (`controller`, `service/impl`, `mapper`, `model/{entity,dto,vo,enums}`, `exception`, `core`, `langgraph4j`, `ratelimit`, `monitor`, `utils`). Static assets in `src/main/resources/static`.
- Tests: JUnit under `src/test/java` mirroring package structure.
- Frontend (Vite + Vue 3): `ai-code-mother-frontend` with source in `ai-code-mother-frontend/src`.
- SQL: initialization scripts in `sql/` (e.g., `create_table.sql`). Monitoring configs: `prometheus.yml`, `ai_model_grafana_config.json`.

## Build, Test, and Development Commands
- Backend
  - Build + test: `./mvnw clean verify`
  - Run locally: `./mvnw spring-boot:run`
  - Package runnable JAR: `./mvnw -DskipTests package` then `java -jar target/*.jar`
- Frontend
  - Install deps: `cd ai-code-mother-frontend && npm ci`
  - Dev server: `npm run dev`
  - Build: `npm run build` | Preview: `npm run preview`
  - Lint/format/type-check: `npm run lint` | `npm run format` | `npm run type-check`

## Coding Style & Naming Conventions
- Java: 4-space indent; packages lowercase; classes PascalCase; methods/fields camelCase; constants UPPER_SNAKE_CASE. Prefer constructor injection; keep controllers thin and business logic in services; use DTO/VO for API.
- Vue/TS: 2-space indent; components PascalCase; files kebab-case (e.g., `app-chat-page.vue`); run ESLint/Prettier before PR.

## Testing Guidelines
- Backend: JUnit 5 + Spring Boot Test. Name tests `*Test.java` under matching packages. Cover services/controllers; mock external integrations. Run with `./mvnw test`.
- Frontend: No unit test runner configured; at minimum keep types green (`npm run type-check`) and lint clean. Add e2e/unit tests if introducing complex logic.

## Commit & Pull Request Guidelines
- Commits: Use clear, imperative messages; Conventional Commits (e.g., `feat:`, `fix:`, `docs:`) encouraged. English or Chinese is acceptable—be consistent and specific.
- PRs: Include purpose, scope, screenshots for UI, reproduction steps, and linked issues. Ensure: builds pass, tests added/updated, lint/format run, and SQL/infra changes documented.

## Security & Configuration Tips
- Database: initialize via `sql/create_table.sql`. Configure connection via Spring properties/env. Rate limiting uses Redisson—ensure Redis if enabling.
- Monitoring: optional Prometheus/Grafana configs provided (`prometheus.yml`, `ai_model_grafana_config.json`).
