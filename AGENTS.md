
# Repository Guidelines

## Project Structure & Module Organization
This repository is a multi-module IoT platform. Backend services live in `api-gateway/`, `device-management-service/`, `device-processor-service/`, `analysis-report-service/`, `notification-service/`, `user-service/`, and shared DTO/config code in `common/`. Java sources follow the standard Maven layout under `src/main/java`, with service config in `src/main/resources/application.yml`.

The React dashboard is in `frontend/` with pages under `frontend/src/pages/`, shared UI in `frontend/src/components/`, state stores in `frontend/src/store/`, and API/WebSocket clients in `frontend/src/services/`. Infra and local orchestration files sit at the repo root (`docker-compose*.yml`, `mosquitto/`, `kafka-connect/`, `mock-iot-devices/`, `Makefile`).

## Build, Test, and Development Commands
Use `mvn clean install` at the repo root to build all Java modules. Use `mvn test` for backend tests and `mvn -pl device-management-service test` to target one service.

Use `docker-compose up -d` to start the full stack, or `make up` for the repo-managed startup flow. Run `make smoke` for health checks and basic integration smoke tests.

For the frontend, run `make frontend-setup` once, then `cd frontend && npm start` for local development, `cd frontend && npm test` for React tests, and `cd frontend && npm run build` for a production bundle.

## Coding Style & Naming Conventions
Follow the existing style: 4-space indentation in Java, 2-space indentation in frontend files, `PascalCase` for React components and Java classes, `camelCase` for methods and variables, and lowercase hyphenated names for service directories. Keep controllers thin, push business logic into `service` classes, and place shared contracts in `common/`.

Frontend linting comes from `react-scripts` ESLint defaults. Prefer small, focused components and keep Zustand stores in `frontend/src/store/`.

## Testing Guidelines
JUnit 5 and Mockito are declared for backend testing; place tests under `src/test/java` and use `*Test.java` naming. Frontend tests should live beside components as `*.test.js`. There is no large committed test suite yet, so new features should include focused unit tests plus a quick pass with `make smoke` when service behavior changes.

## Commit & Pull Request Guidelines
Recent history uses Conventional Commit prefixes such as `feat:` and `chore:`. Continue that format and keep subjects specific, for example `feat: add MQTT retry handling`.

Pull requests should summarize affected services, note config or schema changes, link related issues, and include verification steps. Add screenshots for dashboard changes and list any manual API or Docker checks you ran.
