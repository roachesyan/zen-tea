# 奶茶点单系统 - 实施计划 (Planning.md)

## Context

基于 `spec.md` 创建一个奶茶在线点单与后台管理系统。当前项目为绿地项目（仅有 `spec.md`、`CLAUDE.md`），需要从零搭建三个子工程。本计划按 **后端优先** 的顺序实施，确保 API 就绪后再构建前端。

---

## 总体架构

```
zen-tea/
├── admin/          → Spring Boot 3.x 后端 (Java, ~69 文件)
├── admin-front/    → 后台管理前端 (Vue 3 + Ant Design Vue, ~38 文件)
├── client/         → 移动端点单前端 (Vue 3 + Vant UI, ~40 文件)
├── spec.md
└── CLAUDE.md
```

**总计约 150 个文件，20 个 API 端点。**

---

## Phase 1: 后端服务 (admin/)

### 1.1 项目骨架与配置

| # | 文件 | 用途 |
|---|------|------|
| 1 | `pom.xml` | Maven 构建，依赖：Spring Boot 3.3.x, MyBatis-Plus 3.5.5, SQLite JDBC, JJWT 0.12.x, springdoc-openapi, Lombok |
| 2 | `ZenteaApplication.java` | 主启动类 |
| 3 | `application.yml` | 端口 8080, SQLite 路径, JWT 配置 (access 15min, refresh 7d) |
| 4 | `MybatisPlusConfig.java` | 注册 `PaginationInnerInterceptor(DbType.SQLITE)` |
| 5 | `WebMvcConfig.java` | CORS (允许 localhost:5173/5174), MetaObjectHandler 自动填充时间 |
| 6 | `logback-spring.xml` | 控制台 + 文件日志 |

### 1.2 数据库 Schema

**文件：** `admin/src/main/resources/schema.sql`

6 张表 + 索引：

| 表名 | 字段 | 说明 |
|------|------|------|
| `user` | id, username, password(BCrypt), nickname, phone, avatar_url, role(CUSTOMER/ADMIN), status, create_time, update_time | 用户表 |
| `refresh_token` | id, user_id, token, expire_time, create_time | Token 刷新表 |
| `category` | id, name, icon, sort, status, create_time, update_time | 分类表 |
| `product` | id, category_id, name, price(DECIMAL), image_url, description, status(上下架), sort, create_time, update_time | 商品表 |
| `orders` | id, order_no, user_id, total_amount, status(PENDING/MAKING/DONE/CANCELLED), remark, create_time, update_time | 订单主表 |
| `order_item` | id, order_id, product_id, product_name(快照), quantity, price(快照), specs(JSON), subtotal, create_time | 订单明细表 |

**索引：** `idx_product_category_id`, `idx_product_status`, `idx_orders_user_id`, `idx_orders_status`, `idx_orders_create_time`, `idx_order_item_order_id`, `idx_user_username`

**种子数据 (`data.sql`)：** 1 个管理员账号 + 6 个默认分类 + 12-18 个示例商品

### 1.3 通用基础设施

| 文件 | 职责 |
|------|------|
| `Result<T>` | 统一响应包装 (code, message, data) |
| `ResultCode` | 状态码枚举 (200/201/400/401/403/404/500) |
| `BusinessException` | 业务异常类 |
| `GlobalExceptionHandler` | 全局异常拦截 @RestControllerAdvice |
| `Constants` | 全局常量 |
| `OrderNoGenerator` | 订单号生成工具 |

### 1.4 实体类

`User`, `RefreshToken`, `Category`, `Product`, `Orders`, `OrderItem` — 使用 MyBatis-Plus `@TableName`, `@TableId(type=AUTO)` 注解。

### 1.5 Mapper 层

6 个 Mapper 接口，均继承 `BaseMapper<Entity>`：`UserMapper`, `RefreshTokenMapper`, `CategoryMapper`, `ProductMapper`, `OrderMapper`, `OrderItemMapper`

### 1.6 DTO 层

**请求 DTO (10个)：**
`RegisterRequest`, `LoginRequest`, `RefreshTokenRequest`, `CategoryCreateRequest`, `CategoryUpdateRequest`, `ProductCreateRequest`, `ProductUpdateRequest`, `OrderCreateRequest`, `OrderItemCreateRequest`, `OrderStatusUpdateRequest`

**响应 DTO (7个)：**
`LoginResponse`, `UserInfoResponse`, `CategoryResponse`, `ProductResponse`, `OrderResponse`, `OrderItemResponse`, `DashboardResponse`

### 1.7 安全层 (JWT)

| 文件 | 职责 |
|------|------|
| `JwtTokenProvider` | 生成/解析/验证 access + refresh token |
| `JwtAuthenticationFilter` | OncePerRequestFilter, 提取 Bearer token |
| `SecurityUser` | UserDetails 实现 |
| `SecurityUtils` | 获取当前用户/角色工具方法 |
| `SecurityConfig` | 无状态安全链，公开接口: `/api/auth/**`, `GET /api/products`, `GET /api/categories` |

### 1.8 Service 层

| Service | 核心方法 |
|---------|----------|
| `AuthService` | register, login, refreshToken, logout |
| `UserService` | getById, getByUsername, updateProfile |
| `CategoryService` | list, create, update, delete, updateSort |
| `ProductService` | page, listByCategory, create, update, updateStatus, delete |
| `OrderService` | create(@Transactional), pageByUser, pageAll, updateStatus, getDashboardStats |

**关键事务：** `OrderService.create()` 验证商品 → 计算总价 → 生成订单号 → 插入 orders + order_item

### 1.9 Controller 层

| Controller | 端点 |
|------------|------|
| `AuthController` | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout` |
| `CategoryController` | `GET/POST/PUT/DELETE /api/categories` |
| `ProductController` | `GET/POST/PUT/DELETE /api/products`, `PATCH /api/products/{id}/status` |
| `OrderController` | `POST/GET /api/orders`, `GET /api/orders/{id}`, `PATCH /api/orders/{id}/status`, `GET /api/orders/dashboard/stats` |

### 1.10 测试

- Service 层单元测试 (Mock DAO): Auth, Category, Product, Order
- Controller 层集成测试 (MockMvc + @SpringBootTest)
- 覆盖率目标 ≥ 80%

### 后端包结构

```
admin/src/main/java/com/zentea/
├── ZenteaApplication.java
├── config/           (MybatisPlusConfig, WebMvcConfig, SecurityConfig, SwaggerConfig)
├── common/
│   ├── result/       (Result, ResultCode)
│   ├── exception/    (BusinessException, GlobalExceptionHandler)
│   ├── constant/     (Constants)
│   └── util/         (OrderNoGenerator)
├── security/         (JwtTokenProvider, JwtAuthenticationFilter, SecurityUser, SecurityUtils)
├── entity/           (User, RefreshToken, Category, Product, Orders, OrderItem)
├── mapper/           (6个 Mapper 接口)
├── dto/
│   ├── request/      (10个请求 DTO)
│   └── response/     (7个响应 DTO)
├── service/
│   ├── (5个接口)
│   └── impl/         (5个实现)
└── controller/       (4个 Controller)
```

---

## Phase 2: 后台管理前端 (admin-front/)

### 2.1 技术栈

Vue 3 + Vite + Pinia + Ant Design Vue + TypeScript + Bun

### 2.2 页面路由

| 路由 | 页面 | 功能 |
|------|------|------|
| `/login` | 登录页 | 管理员登录 |
| `/dashboard` | 数据看板 | 今日订单数、营业额、最近订单 |
| `/category` | 分类管理 | 分类列表 CRUD + 拖拽排序 |
| `/product` | 商品管理 | 商品列表 + 上下架 + 图片上传 |
| `/order` | 订单管理 | 订单列表 + 状态流转 |

### 2.3 目录结构

```
admin-front/src/
├── api/              (request.ts, auth.ts, category.ts, product.ts, order.ts, dashboard.ts)
├── assets/styles/    (variables.less, global.less)
├── components/
│   ├── common/       (PageHeader, StatusTag)
│   ├── category/     (CategoryForm)
│   ├── product/      (ProductForm, ProductStatusSwitch)
│   └── order/        (OrderStatusSelect)
├── composables/      (useTable, useFormModal)
├── layouts/          (AdminLayout, SideMenu, HeaderBar)
├── router/           (index.ts, routes.ts)
├── stores/           (auth.ts, app.ts)
├── types/            (api, category, product, order, dashboard)
├── utils/            (auth.ts, format.ts)
├── views/
│   ├── LoginView.vue
│   ├── DashboardView.vue
│   ├── category/CategoryView.vue
│   ├── product/ProductView.vue
│   └── order/OrderView.vue
├── App.vue
└── main.ts
```

### 2.4 核心交互

- 经典中后台布局：侧边栏 + Header + 面包屑 + 内容区
- a-table 配合分页、筛选
- a-form 校验 + a-modal 弹窗 CRUD
- Axios 拦截器：JWT 自动携带、401 自动刷新、统一错误提示

---

## Phase 3: 移动端 (client/)

### 3.1 技术栈

Vue 3 + Vite + Pinia + Vant UI + TypeScript + Bun + postcss-px-to-viewport

### 3.2 页面路由

| 路由 | 页面 | 功能 |
|------|------|------|
| `/home` | 首页 | 轮播图、推荐茶饮、分类快捷入口 |
| `/menu` | 菜单页 | 左侧分类导航 + 右侧商品列表 + 规格选择 |
| `/cart` | 购物车 | 商品数量调整、总价计算、去结算 |
| `/checkout` | 结算页 | 确认订单、备注、模拟支付 |
| `/order` | 订单列表 | Tab 状态筛选、订单详情展开 |
| `/login` | 登录页 | 用户登录/注册 |

### 3.3 目录结构

```
client/src/
├── api/              (request.ts, auth.ts, product.ts, order.ts)
├── assets/
│   ├── styles/       (variables.scss, global.scss)
│   └── images/
├── components/
│   ├── common/       (AppHeader, AppTabbar, EmptyState)
│   ├── product/      (ProductCard, ProductList, SpecSelector)
│   ├── cart/         (CartItem, CartSummary)
│   └── order/        (OrderCard, OrderStatusTag)
├── composables/      (useAuth, useCart, useLoading)
├── layouts/          (MainLayout)
├── router/           (index.ts)
├── stores/           (auth.ts, cart.ts[持久化], product.ts)
├── types/            (api, product, order, user)
├── utils/            (format.ts, storage.ts)
├── views/
│   ├── HomeView.vue
│   ├── MenuView.vue
│   ├── CartView.vue
│   ├── CheckoutView.vue
│   ├── OrderView.vue
│   └── LoginView.vue
├── App.vue
└── main.ts
```

### 3.4 核心交互

- 底部 TabBar：首页、菜单、购物车(带角标)、订单
- 规格选择器 (SpecSelector)：冰度、甜度、加料 → van-radio-group / van-checkbox-group
- 购物车 Pinia 持久化（pinia-plugin-persistedstate）
- 每个异步页面处理 loading / empty / error / disabled 四态

### 3.5 规格数据结构

```typescript
interface ProductSpecs {
  ice: 'normal' | 'less' | 'no_ice' | 'hot';
  sweetness: 'full' | 'less' | 'half' | 'little' | 'none';
  toppings: string[];  // ['pearl', 'coconut_jelly', 'taro_ball', ...]
}
```

---

## Phase 4: 集成测试与交付

### 4.1 后端验证
- `./mvnw spring-boot:run` 启动，验证 schema.sql 自动建表
- Swagger UI (`/swagger-ui.html`) 测试全部 20 个端点
- JWT 完整流程：注册 → 登录 → 带 Token 请求 → 刷新 Token

### 4.2 Admin-Front 集成
- 管理员登录 → 分类 CRUD → 商品管理 → 订单状态流转 → 看板数据展示

### 4.3 Client 集成
- 浏览菜单 → 选择规格 → 加入购物车 → 结算下单 → 查看订单

### 4.4 构建验证
- `./mvnw package` (后端)
- `bun run build` (两个前端)
- CORS、环境变量、生产构建

---

## 关键设计决策

1. **订单规格用 JSON 字符串存储**：简单灵活，无需额外的规格表
2. **订单明细快照商品名/价格**：防止商品调价影响历史订单
3. **JWT 双 Token 方案**：access 15min + refresh 7d，refresh_token 表支持服务端撤销
4. **状态用 VARCHAR 而非整数**：可读性好（PENDING/MAKING/DONE/CANCELLED）
5. **SQLite WAL 模式**：如遇并发写问题，启用 `PRAGMA journal_mode=WAL`

---

## 实施优先级

```
Phase 1 (后端) → Phase 2 (Admin-Front) → Phase 3 (Client) → Phase 4 (集成)
     ↓                  ↓                       ↓
  API 就绪          管理功能可用            用户点单可用
```

每个 Phase 内部按层自底向上：Entity → Mapper → DTO → Service → Controller → 测试
