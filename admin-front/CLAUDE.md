# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Admin panel (desktop) for a bubble tea ordering system. Part of a monorepo at `../` alongside a `client/` (mobile ordering) and `admin/` (Spring Boot backend).

## Build & Run Commands

```bash
bun install          # Install dependencies
bun run dev          # Dev server on port 5173
bun run build        # Type-check (vue-tsc) then production build
bun run preview      # Preview production build locally
```

No test runner or linter is configured yet.

## Architecture

**Stack**: Vue 3 Composition API + TypeScript + Vite + Pinia + Ant Design Vue 4 + Less

**Backend proxy**: Vite dev server proxies `/api` to `http://localhost:8080` (the Spring Boot backend must be running).

### Request Flow

All API calls go through `src/api/request.ts` — an Axios instance with:
- JWT `Authorization: Bearer` header injection from localStorage
- Automatic token refresh on 401 (queues concurrent requests during refresh)
- Response interceptor unwraps `Result<T>` envelope; rejects on non-200/201 codes

### API Layer Pattern

Each domain module in `src/api/` (auth, category, product, order) exports plain functions that call `request` and return typed `Result<T>` promises. Example:

```ts
import request from './request'
import type { Result, Category } from '@/types'

export function getCategories() {
  return request.get<Result<Category[]>>('/categories')
}
```

When adding a new resource, create `src/api/{resource}.ts`, `src/types/{resource}.ts`, re-export from `src/types/index.ts`, and add the view + route.

### Type System

- `Result<T>` — API response envelope (`code`, `message`, `data`)
- `PageResult<T>` — paginated list response (`records`, `total`, `size`, `current`, `pages`)
- Domain types in `src/types/` with separate request/response interfaces (e.g., `Category`, `CategoryCreateRequest`, `CategoryUpdateRequest`)

### Routing & Auth

- Route guard in `src/router/index.ts` checks `getToken()` (localStorage); redirects unauthenticated users to `/login`
- Auth state managed by `useAuthStore` (Pinia) — stores user info and token
- Tokens use keys `zentea_access_token` / `zentea_refresh_token` in localStorage

### State Management

- `stores/auth.ts` — authentication (login, logout, user info)
- `stores/app.ts` — UI state (sidebar collapsed toggle)

### Layout Structure

`AdminLayout.vue` wraps all authenticated routes with a sidebar (`SideMenu.vue`) and header (`HeaderBar.vue`). Feature views live under `src/views/{module}/`.

## Conventions

- Path alias: `@` → `src/`
- Styling: Less with variables in `assets/styles/variables.less`
- API paths: plural resource nouns (`/categories`, `/products`, `/orders`)
- All backend responses are wrapped in `Result<T>` — never consume raw Axios responses without unwrapping
