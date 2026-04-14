# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bubble tea (奶茶) ordering system with three separate sub-projects in a monorepo:

| Module | Directory | Tech Stack | Purpose |
|--------|-----------|------------|---------|
| **Client** | `client/` | Vue 3 + Vite + Pinia + Vant UI + TypeScript (Bun) | Mobile-first customer ordering |
| **Admin** | `admin/` | Spring Boot 3.x + SQLite + MyBatis-Plus + Java | Backend REST API |
| **Admin-Front** | `admin-front/` | Vue 3 + Vite + Pinia + Ant Design Vue + TypeScript (Bun) | Desktop admin panel |

## Build & Run Commands

### Client (Mobile Frontend)
```bash
cd client
bun install
bun run dev          # Dev server
bun run build        # Production build
bun run lint         # ESLint + Prettier
```

### Admin (Backend)
```bash
cd admin
./mvnw spring-boot:run          # Run backend
./mvnw test                      # Run tests
./mvnw compile                   # Compile only
```

### Admin-Front (Admin Panel)
```bash
cd admin-front
bun install
bun run dev          # Dev server
bun run build        # Production build
bun run lint         # ESLint + Prettier
```

## Architecture

- **RESTful API**: All endpoints follow `/api/{resource}` convention (e.g., `GET /api/products`, `POST /api/orders`)
- **Database**: SQLite — auto-initialized on first startup if `.db` file is missing, using `schema.sql` at project root
- **Unified API Response**: Backend wraps all responses in `Result<T>` (`code`, `message`, `data`) with `@RestControllerAdvice` for global exception handling
- **State Management**: Both frontends use Pinia with persistence (cart state persisted in client)
- **HTTP Client**: Axios with request/response interceptors for error handling

## Database Schema

Four core tables: `category`, `product`, `orders`, `order_item`. MyBatis-Plus `IService`/`BaseMapper` for CRUD, `PaginationInnerInterceptor` for paginated queries.

## Code Conventions

- **Frontend**: ESLint + Prettier enforced, Bun as package manager
- **Backend**: Follow Alibaba Java Development Guidelines
- **API responses**: Always use the `Result<T>` wrapper — never return raw objects from controllers

## Backend Quality Requirements

### Layering
- Strict **Controller → Service → DAO/Mapper** three-tier architecture. No cross-layer calls.
- Controller handles only parameter validation and response wrapping; all business logic goes to Service.

### RESTful API
- Plural resource nouns (`/api/products`, `/api/orders`)
- Correct HTTP methods (GET/POST/PUT/PATCH/DELETE) and status codes (200/201/400/401/403/404/500)
- Provide Swagger/OpenAPI documentation

### Authentication
- **JWT** based auth: `Authorization: Bearer <token>` header
- Role-based access: customer (ordering) vs admin (management)
- Implement register, login, token validation, token refresh

### Exception Handling
- Custom `BusinessException` with error code and message
- Global `@RestControllerAdvice` returning unified `Result<T>`
- Distinguish validation errors, business errors, and system errors

### Logging
- **SLF4J + Logback** — never `System.out.println`
- Levels: ERROR (system), WARN (business), INFO (key operations), DEBUG (debugging)
- Audit logs for critical operations (who, when, what, result)

### Testing
- JUnit 5 + Mockito for unit/integration tests
- Service layer: Mock DAO, test business logic
- Controller layer: `@SpringBootTest` + `MockMvc`
- Coverage target **≥ 80%**

### Database
- Analyze execution plans for complex queries
- Use MyBatis-Plus `LambdaQueryWrapper` for joins
- Index design covering high-frequency query fields

### Transactions
- `@Transactional` on multi-table Service methods
- Specify propagation (default `REQUIRED`) and rollback rules (rollback on `Exception`)
- `@Transactional(readOnly = true)` for read-only operations

## Frontend Quality Requirements

### User-Task Thinking
- Design pages from **user operation paths**, not just API calls
- Every interaction must answer: **why the user clicks → what they see after → what happens on failure**

### State Management
- Distinguish four state types: **local** (component), **derived** (computed), **async** (request lifecycle), **global** (Pinia store)
- Single source of truth — avoid state duplication and cross-contamination
- Every async page must handle **loading / empty / error / disabled** states

### Component Boundaries
- Split by responsibility: **display** (pure UI), **container** (logic + data), **generic** (reusable)
- Reasonable Props, events, and type definitions
- Single component **≤ 300 lines** — no page-level monoliths

### Async Interaction
- Handle request lifecycle: initiation, cancellation (on unmount), concurrency, retry, error display
- Prevent stale data overwriting fresh data (race conditions)
- Ensure consistency across filter/pagination/condition switches

### Page Experience
- Handle all states: empty, error (with retry), permission denied, skeleton loading, disabled
- Provide operation feedback (Toast/notifications), clear information hierarchy
- Goal: **user completes the task, not just sees data**

### Styling & Layout
- Follow Vant (mobile) / Ant Design Vue (desktop) design systems
- Responsive, proper spacing, alignment — visually deliverable, not just functional

### Quality Assurance
- Component-level, page-level, and interaction-level self-testing
- Unit/integration tests for critical flows
- Verify logic, state, and interaction before committing

### End-to-End Delivery
- Each feature module: implementation → integration testing → self-testing → fixes → delivery
- Traceable deliverables: design understanding → implementation notes → test records → fix logs

## Documentation Requirements
- Detailed design document (module design, API design, database schema)
- Database schema docs with field descriptions, indexes, foreign keys, ER diagram
