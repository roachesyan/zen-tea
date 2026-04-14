# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
bun install            # Install dependencies
bun run dev            # Dev server on port 5174 (proxies /api → localhost:8080)
bun run build          # Type-check (vue-tsc) then production build
bun run preview        # Preview production build
```

No ESLint/Prettier config is set up yet. The root CLAUDE.md references `bun run lint` but the script does not exist in this package.

## Architecture

Mobile-first bubble tea ordering app built with Vue 3 Composition API (`<script setup>`), Vant UI, and TypeScript.

### Key Technical Decisions

- **PostCSS-px-to-viewport** converts all `px` to `vw` units based on a 375px design width. Vant components are included in this conversion. Write CSS in `px` targeting a 375px mobile viewport.
- **Vant auto-import** via `unplugin-vue-components` — do not manually import Vant components; they are resolved automatically.
- **Path alias**: `@` maps to `src/`.
- **Pinia persistence** via `pinia-plugin-persistedstate`. Stores opt in with `persist: true` (see `cart` store). Auth tokens are stored separately in `localStorage` via `src/utils/auth.ts`.

### API Layer

- **Axios instance** in `src/api/request.ts` — baseURL `/api`, 15s timeout.
- All backend responses use `Result<T>` envelope: `{ code: number, message: string, data: T }`. Success codes are 200/201. The response interceptor rejects non-success codes.
- **Token refresh**: On 401, the interceptor queues concurrent requests while refreshing the token via `POST /api/auth/refresh`, then replays them. If refresh fails, tokens are cleared and user is redirected to `/login`.
- API modules (`src/api/auth.ts`, `product.ts`, `order.ts`) wrap the axios instance with typed calls.

### Routing

- `MainLayout.vue` wraps the four tab pages: Home, Menu, Cart, Order.
- Route guard in `src/router/index.ts` checks `meta.requiresAuth` and redirects to `/login?redirect=<path>` if no token exists.
- Checkout is the only protected route currently.

### State Management (Pinia Stores)

| Store | Purpose | Persisted? |
|-------|---------|------------|
| `auth` | User info, JWT tokens, login/register/logout | No (tokens in localStorage via utils) |
| `cart` | Cart items with specs, totals | Yes |
| `product` | Categories and products, loading state | No |

### Type System

Types are in `src/types/` with a barrel export in `index.ts`. Key types:
- `Result<T>`, `PageResult<T>` — API response envelopes
- `UserInfo`, `LoginRequest`, `RegisterRequest`, `LoginResponse` — auth
- `Category`, `Product` — catalog
- `Order`, `OrderItem`, `OrderCreateRequest`, `ProductSpecs` — ordering
- `CartItem` — cart state

### Component Organization

```
src/components/
  common/     EmptyState — reusable empty/error placeholder
  product/    ProductCard, SpecSelector
  cart/       CartItem
  order/      OrderCard
src/views/    Page-level components (one per route)
src/layouts/  MainLayout with bottom tab bar
```

Components use Composition API with `<script setup>`, typed props, and scoped SCSS.

### Styling

- SCSS with variables in `src/assets/styles/variables.scss` (`$primary-color: #1989fa`, `$price-color: #ee0a24`, etc.)
- Global styles in `src/assets/styles/global.scss`
- Use Vant's design system as the foundation; customize via CSS variables when needed.

### Backend Dependency

This app requires the backend (`admin/`) running on `localhost:8080`. The Vite dev server proxies `/api` requests to it.
