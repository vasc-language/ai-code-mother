# Repository Guidelines

## Project Structure & Module Organization
- Backend (Spring Boot, Maven): `src/main/java/com/spring/aicodemother` organized by layer: `controller`, `service/impl`, `mapper`, `model/{entity,dto,vo,enums}`, `exception`, `core`, `langgraph4j`, `ratelimit`, `monitor`, `utils`.
- Static assets: `src/main/resources/static`.
- Tests: JUnit under `src/test/java` mirroring backend packages.
- Frontend (Vite + Vue 3): `ai-code-mother-frontend/src`.
- SQL init: `sql/create_table.sql`; monitoring: `prometheus.yml`, `ai_model_grafana_config.json`.

## Build, Test, and Development Commands
- `./mvnw clean verify` — build backend and run tests.
- `./mvnw spring-boot:run` — run backend locally.
- `./mvnw -DskipTests package && java -jar target/*.jar` — package and run JAR.
- `cd ai-code-mother-frontend && npm ci` — install frontend deps.
- `npm run dev` — start Vite dev server.
- `npm run build` / `npm run preview` — build/preview frontend.
- `npm run lint` / `npm run format` / `npm run type-check` — quality checks.

## Coding Style & Naming Conventions
- Java: 4-space indent; packages lowercase; classes PascalCase; methods/fields camelCase; constants UPPER_SNAKE_CASE.
- Prefer constructor injection; keep controllers thin; put business logic in services.
- Use DTO/VO for API boundaries; avoid leaking entities.
- Vue/TS: 2-space indent; components PascalCase; files kebab-case (e.g., `app-chat-page.vue`).
- Run ESLint/Prettier before PRs; keep types green.

## Testing Guidelines
- Frameworks: JUnit 5 + Spring Boot Test.
- Name tests `*Test.java` under matching packages.
- Mock external integrations; keep tests deterministic.
- Run with `./mvnw test`; cover services/controllers at minimum.

## Commit & Pull Request Guidelines
- Commits: clear, imperative; Conventional Commits encouraged (`feat:`, `fix:`, `docs:`). English or Chinese is fine—be consistent.
- PRs: include purpose, scope, linked issues, and screenshots for UI; document SQL/infra changes.
- Ensure: backend builds pass, tests added/updated; frontend lint/format/type-check clean.

## Security & Configuration Tips
- Initialize DB via `sql/create_table.sql`; configure connection via Spring properties/env.
- Rate limiting uses Redisson—ensure Redis if enabling.
- Monitoring is optional; use `prometheus.yml` and `ai_model_grafana_config.json` if needed.

## Agent-Specific Instructions
- Scope: this file applies to the entire repository.
- Keep changes minimal and focused; do not fix unrelated issues.
- Follow existing structure/naming; update docs when behavior changes.
