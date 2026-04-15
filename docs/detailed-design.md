# 禅茶 ZenTea — 详细设计文档

> 版本：1.0 | 更新日期：2026-04-15

---

## 目录

1. [系统概述](#1-系统概述)
2. [系统架构](#2-系统架构)
3. [技术选型](#3-技术选型)
4. [后端服务详细设计 (Admin)](#4-后端服务详细设计-admin)
5. [客户端详细设计 (Client)](#5-客户端详细设计-client)
6. [管理后台详细设计 (Admin-Front)](#6-管理后台详细设计-admin-front)
7. [认证与安全](#7-认证与安全)
8. [统一响应与异常处理](#8-统一响应与异常处理)
9. [API 接口设计](#9-api-接口设计)
10. [部署说明](#10-部署说明)

---

## 1. 系统概述

禅茶（ZenTea）是一个奶茶在线点单系统，采用前后端分离的 Monorepo 架构，包含三个独立子项目：

| 子项目 | 目录 | 定位 | 用户 |
|--------|------|------|------|
| Admin | `admin/` | 后端 REST API 服务 | 系统 |
| Client | `client/` | 移动端客户下单页面 | 消费者 |
| Admin-Front | `admin-front/` | 桌面端管理后台 | 店铺管理员 |

### 核心业务流程

```
消费者 (Client)                    管理员 (Admin-Front)
    │                                    │
    ├─ 注册/登录 ──→ Auth API            ├─ 登录 ──→ Auth API
    ├─ 浏览分类/商品 ──→ Category/Product  ├─ 管理分类 ──→ Category CRUD
    ├─ 选择规格/加购物车 (本地)           ├─ 管理商品 ──→ Product CRUD
    ├─ 提交订单 ──→ Order API            ├─ 处理订单 ──→ Order Status
    └─ 查看订单状态 ──→ Order API         └─ 查看数据统计 ──→ Dashboard
```

---

## 2. 系统架构

### 2.1 整体架构图

```
┌──────────────┐  ┌──────────────┐
│   Client     │  │  Admin-Front │
│  (Vue 3 +    │  │  (Vue 3 +    │
│   Vant UI)   │  │  Ant Design) │
│  Port: 5174  │  │  Port: 5173  │
└──────┬───────┘  └──────┬───────┘
       │ http proxy /api  │
       ▼                  ▼
┌──────────────────────────────┐
│       Admin (Backend)        │
│   Spring Boot 3.3.6          │
│   Port: 8080                 │
│                              │
│  ┌──────────┐ ┌───────────┐ │
│  │Security  │ │Controller │ │
│  │Filter    │→│ Layer     │ │
│  └──────────┘ └─────┬─────┘ │
│                     │        │
│               ┌─────▼─────┐  │
│               │ Service   │  │
│               │ Layer     │  │
│               └─────┬─────┘  │
│                     │        │
│               ┌─────▼─────┐  │
│               │ Mapper    │  │
│               │ (MyBatis) │  │
│               └─────┬─────┘  │
│                     │        │
│               ┌─────▼─────┐  │
│               │  SQLite   │  │
│               │ zentea.db │  │
│               └───────────┘  │
└──────────────────────────────┘
```

### 2.2 请求处理流程

```
HTTP Request
  → JwtAuthenticationFilter（Token 校验）
    → Controller（参数校验 @Valid）
      → Service（业务逻辑 @Transactional）
        → Mapper（MyBatis-Plus LambdaQueryWrapper）
          → SQLite
  ← GlobalExceptionHandler → Result<T> JSON 响应
```

---

## 3. 技术选型

### 3.1 后端 (Admin)

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行时 |
| Spring Boot | 3.3.6 | 应用框架 |
| MyBatis-Plus | 3.5.5 | ORM |
| SQLite | - | 嵌入式数据库 |
| Spring Security | 6.x | 认证授权 |
| JWT (jjwt) | - | Token 生成/验证 |
| BCrypt | - | 密码加密 |
| Swagger/OpenAPI | - | API 文档 |
| Lombok | - | 减少样板代码 |

### 3.2 客户端 (Client)

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.32 | UI 框架 |
| Vant | 4.9.24 | 移动端 UI 组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Axios | 1.15.0 | HTTP 请求 |
| Vue Router | 4.x | 路由 |
| TypeScript | 6.0.2 | 类型安全 |
| Vite | 8.0.4 | 构建工具 |
| PostCSS-px-to-viewport | 8 | 移动端适配 |
| Sass | 1.99.0 | CSS 预处理器 |

### 3.3 管理后台 (Admin-Front)

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.32 | UI 框架 |
| Ant Design Vue | 4.x | 桌面端 UI 组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Axios | 1.15.0 | HTTP 请求 |
| Vue Router | 4.x | 路由 |
| TypeScript | 6.0.2 | 类型安全 |
| Vite | 8.0.4 | 构建工具 |
| Less | 4.6.4 | CSS 预处理器 |

---

## 4. 后端服务详细设计 (Admin)

### 4.1 包结构

```
com.zentea
├── common/
│   ├── constant/         # Constants — 角色名、状态常量
│   ├── result/           # Result<T>, ResultCode 枚举
│   └── exception/        # BusinessException
├── config/
│   ├── SecurityConfig    # Spring Security + JWT 过滤器链
│   ├── MybatisPlusConfig # 分页插件 + ID 生成器
│   ├── SwaggerConfig     # OpenAPI 文档配置
│   ├── WebMvcConfig      # CORS + 时间戳自动填充
│   └── JwtProperties     # JWT 配置属性
├── controller/
│   ├── AuthController    # /api/auth/**
│   ├── CategoryController# /api/categories/**
│   ├── ProductController # /api/products/**
│   └── OrderController   # /api/orders/**
├── dto/
│   ├── request/          # 入参 DTO（带 Jakarta Validation）
│   └── response/         # 出参 DTO（@Builder）
├── entity/               # MyBatis-Plus 实体（@TableName）
├── mapper/               # BaseMapper 接口
├── security/
│   ├── JwtTokenProvider  # Token 生成/解析/验证
│   ├── JwtAuthenticationFilter # 请求拦截提取 Token
│   ├── SecurityUser      # UserDetails 实现
│   └── SecurityUtils     # 获取当前用户工具
├── service/              # 业务接口
│   └── impl/             # @Service 实现
└── ZenteaApplication     # 启动类
```

### 4.2 分层设计

#### Controller 层

- 职责：参数接收、校验触发（`@Valid`）、响应包装（`Result.ok()`）
- 不包含任何业务逻辑
- 使用 Swagger `@Tag`、`@Operation` 注解生成文档

#### Service 层

- 接口继承 `IService<Entity>`
- 实现类标注 `@Service`、`@Slf4j`
- 写操作标注 `@Transactional`（默认 REQUIRED，rollbackOn Exception）
- 只读操作标注 `@Transactional(readOnly = true)`
- Entity → Response DTO 转换在 Service 层完成，Entity 不暴露给 Controller

#### Mapper 层

- 接口继承 `BaseMapper<Entity>`，标注 `@Mapper`
- 不使用 XML，全部使用 `LambdaQueryWrapper` 构建查询
- MyBatis-Plus 自动映射 snake_case → camelCase

### 4.3 核心业务逻辑

#### 4.3.1 认证流程 (AuthService)

```
注册: register(username, password, nickname?, phone?)
  → 校验用户名唯一 → BCrypt 加密密码 → 插入 user 表 → 生成 JWT → 返回 LoginResponse

登录: login(username, password)
  → 查询用户 → BCrypt 校验密码 → 生成 accessToken (15min) + refreshToken (7d)
  → 存储 refreshToken 到 refresh_token 表 → 返回 LoginResponse

刷新: refreshToken(token)
  → 查询 refresh_token 表校验有效性和过期时间 → 生成新 accessToken → 返回

登出: logout(userId)
  → 删除 refresh_token 表中该用户所有记录
```

#### 4.3.2 商品管理 (ProductService)

```
分页查询: page(pageNum, pageSize, categoryId?, keyword?, status?)
  → LambdaQueryWrapper 条件拼接 → MyBatis-Plus 分页查询 → 转换为 ProductResponse（含 categoryName）

按分类列表: listByCategory(categoryId?)
  → 查询上架商品(status=1)，按 sort 排序

创建: create(request)
  → 校验 categoryId 存在 → 插入 product → 返回 ProductResponse

状态切换: updateStatus(id, status)
  → 0=下架, 1=上架 → 更新 status 字段
```

#### 4.3.3 订单处理 (OrderService)

```
创建订单: create(userId, request)
  → 遍历 items → 校验商品存在且上架 → 计算 subtotal → 生成订单号(ZN+时间戳+4位序列)
  → 插入 orders + order_item → 返回 OrderResponse

用户订单列表: pageByUser(userId, pageNum, pageSize, status?)
  → 按 user_id 过滤 → 分页 → 关联查询 order_item

管理员订单列表: pageAll(pageNum, pageSize, status?)
  → 全量查询 → 关联 user 表获取 username

状态流转: updateStatus(id, status)
  → PENDING → MAKING → DONE / CANCELLED
  → 校验当前状态允许流转到目标状态

数据看板: getDashboardStats()
  → 聚合查询: 今日订单数、今日营收、总订单数、总营收、待处理订单数
```

#### 4.3.4 订单号生成 (OrderNoGenerator)

格式：`ZN` + `yyyyMMddHHmmss` + 4 位序列号，示例：`ZN202604151430250001`

### 4.4 配置项

```yaml
# application.yml 关键配置
server:
  port: 8080

spring:
  datasource:
    url: jdbc:sqlite:${zentea.db.path:./zentea.db}

zentea:
  db:
    path: ./zentea.db          # 数据库文件路径（可通过 ZENTEA_DB_PATH 环境变量覆盖）
  jwt:
    secret: ${JWT_SECRET}      # JWT 签名密钥
    access-token-expiration: 900000   # 15 分钟
    refresh-token-expiration: 604800000 # 7 天
```

---

## 5. 客户端详细设计 (Client)

### 5.1 目录结构

```
client/src/
├── api/              # API 接口模块
│   ├── request.ts    # Axios 实例 + 拦截器
│   ├── auth.ts       # 认证接口
│   ├── product.ts    # 商品接口
│   └── order.ts      # 订单接口
├── assets/styles/    # 全局样式
│   ├── variables.scss # SCSS 变量
│   └── global.scss   # 全局样式
├── components/       # 可复用组件
│   ├── common/       # EmptyState
│   ├── product/      # ProductCard, SpecSelector
│   ├── cart/         # CartItem
│   └── order/        # OrderCard
├── layouts/          # MainLayout（底部 Tab 导航）
├── router/           # 路由配置 + 守卫
├── stores/           # Pinia Store
│   ├── auth.ts       # 认证状态
│   ├── cart.ts       # 购物车（持久化）
│   └── product.ts    # 商品/分类
├── types/            # TypeScript 类型定义
├── utils/            # 工具函数
│   ├── auth.ts       # Token 存取
│   └── format.ts     # 格式化
└── views/            # 页面组件
    ├── HomeView      # 首页
    ├── MenuView      # 菜单
    ├── CartView      # 购物车
    ├── OrderView     # 订单列表
    ├── LoginView     # 登录/注册
    └── CheckoutView  # 结算页
```

### 5.2 页面设计

#### 首页 (HomeView — `/home`)

| 区域 | 组件 | 说明 |
|------|------|------|
| 顶部导航 | `van-nav-bar` | 标题 "禅茶 ZenTea" |
| 轮播 Banner | `van-swipe` | 3 张促销 Banner，渐变色背景 |
| 分类网格 | 4 列 grid | 展示全部分类，点击跳转 `/menu?categoryId=` |
| 推荐茶饮 | `ProductCard` 列表 | 展示前 6 个商品，支持直接选规格加入购物车 |

#### 菜单页 (MenuView — `/menu`)

| 区域 | 组件 | 说明 |
|------|------|------|
| 顶部导航 | `van-nav-bar` | 标题 "菜单" |
| 左侧分类 | `van-sidebar` | 90px 宽，点击切换分类，带分类名称和商品数量 |
| 右侧商品 | `ProductCard` 列表 | 当前分类下的商品列表 |
| 规格选择 | `SpecSelector` (ActionSheet) | 选冰度/甜度/小料后加入购物车 |

#### 购物车页 (CartView — `/cart`)

| 区域 | 组件 | 说明 |
|------|------|------|
| 商品列表 | `CartItem` 组件 | 图片 + 名称 + 规格 + 数量步进器 + 小计 |
| 底部结算栏 | `van-submit-bar` | 显示总价，点击去结算 |
| 空状态 | `EmptyState` | 购物车为空时展示 |

#### 订单页 (OrderView — `/order`)

| 区域 | 组件 | 说明 |
|------|------|------|
| 状态 Tab | `van-tabs` | 全部/待制作/制作中/已完成/已取消 |
| 订单卡片 | `OrderCard` 列表 | 订单号、状态、商品摘要、总价、时间 |
| 订单详情 | ActionSheet | 点击卡片查看完整订单明细 |

#### 登录页 (LoginView — `/login`)

| 区域 | 组件 | 说明 |
|------|------|------|
| Logo 区域 | 品牌 Logo + 应用名 | 茶饮品牌视觉 |
| 表单 | `van-form` | 登录/注册切换，用户名 + 密码 |
| 提交 | `van-button` | 登录或注册，成功后跳转 |

#### 结算页 (CheckoutView — `/checkout`)

| 区域 | 组件 | 说明 |
|------|------|------|
| 订单摘要 | 商品列表 | 确认购物车中的商品 |
| 备注 | `van-field` textarea | 订单备注输入 |
| 提交 | `van-button` | 提交订单，成功后清空购物车 |

### 5.3 状态管理设计

#### Auth Store (`stores/auth.ts`)

```
State:
  user: UserInfo | null       // 当前用户信息
  token: string | null        // 访问令牌
  isLoggedIn: computed        // 是否已登录

Actions:
  login(username, password)   // 登录 → 存 token + user
  register(data)              // 注册 → 自动登录
  logout()                    // 清空 token + user
  setUser(userInfo)           // 设置用户信息

持久化: 否（Token 通过 localStorage 独立管理）
```

#### Cart Store (`stores/cart.ts`)

```
State:
  items: CartItem[]           // 购物车商品列表
  totalCount: computed        // 商品总数量
  totalPrice: computed        // 总价

Actions:
  addItem(id, name, price, image, specs)  // 添加商品（相同规格合并数量）
  removeItem(index)                        // 移除商品
  updateQuantity(index, quantity)          // 更新数量
  clearCart()                              // 清空购物车

持久化: 是（pinia-plugin-persistedstate）
```

#### Product Store (`stores/product.ts`)

```
State:
  categories: Category[]      // 分类列表
  products: Product[]         // 当前商品列表
  loading: boolean            // 加载状态

Actions:
  fetchCategories()           // 获取全部分类
  fetchProducts(categoryId?)  // 获取商品列表

持久化: 否
```

### 5.4 规格选择设计 (SpecSelector)

| 选项类型 | 可选值 | 说明 |
|----------|--------|------|
| 冰度 | normal(正常冰), less(少冰), no_ice(去冰), hot(热饮) | 单选 |
| 甜度 | full(全糖), less(少糖), half(半糖), little(微糖), none(无糖) | 单选 |
| 小料 | 珍珠, 椰果, 芋圆, 红豆 | 多选 |

选择完成后，specs 对象序列化为 JSON 存入 `CartItem.specs`，同时生成可读文本存入 `specsText`。

### 5.5 移动端适配

- 设计稿宽度 375px
- PostCSS-px-to-viewport 自动将 px 转为 vw
- Vant 组件同样纳入转换范围
- 样式使用 SCSS 预处理，变量定义在 `variables.scss`

### 5.6 HTTP 请求层

```
Axios 实例 (request.ts):
  baseURL: /api
  timeout: 15s
  请求拦截器: 注入 Authorization: Bearer <token>
  响应拦截器:
    - 解包 Result<T> → 返回 data
    - 非 200/201 code → reject
    - 401 → 自动刷新 Token（排队重发）→ 失败则清除 Token 跳转 /login
```

---

## 6. 管理后台详细设计 (Admin-Front)

### 6.1 目录结构

```
admin-front/src/
├── api/              # API 接口模块
│   ├── request.ts    # Axios 实例 + 拦截器
│   ├── auth.ts       # 认证接口
│   ├── category.ts   # 分类管理接口
│   ├── product.ts    # 商品管理接口
│   └── order.ts      # 订单管理接口
├── assets/styles/    # 全局样式
│   ├── variables.less # Less 变量
│   └── global.less   # 全局样式
├── components/       # 可复用组件
│   ├── common/       # PageHeader, StatusTag
│   ├── category/     # CategoryForm
│   ├── product/      # ProductForm, ProductStatusSwitch
│   └── order/        # OrderStatusSelect
├── layouts/          # AdminLayout
│   ├── AdminLayout.vue # 主布局
│   ├── SideMenu.vue    # 侧边栏菜单
│   └── HeaderBar.vue   # 顶部导航
├── router/           # 路由配置 + 守卫
├── stores/           # Pinia Store
│   ├── auth.ts       # 认证状态
│   └── app.ts        # UI 状态（侧边栏折叠）
├── types/            # TypeScript 类型定义
├── utils/            # 工具函数
└── views/            # 页面组件
    ├── LoginView     # 登录
    ├── DashboardView # 数据看板
    ├── category/     # 分类管理
    │   └── CategoryView
    ├── product/      # 商品管理
    │   └── ProductView
    └── order/        # 订单管理
        └── OrderView
```

### 6.2 布局设计

```
┌──────────────────────────────────────────────┐
│  HeaderBar                                   │
│  [☰ 折叠] [面包屑导航]            [管理员 ▼] │
├──────────┬───────────────────────────────────┤
│ SideMenu │                                   │
│          │                                   │
│ 数据看板  │         内容区域                   │
│ 分类管理  │        (router-view)              │
│ 商品管理  │                                   │
│ 订单管理  │                                   │
│          │                                   │
└──────────┴───────────────────────────────────┘
```

- 侧边栏：展开 220px，折叠 80px，暗色主题
- 顶部栏：64px 高，含折叠按钮、面包屑、用户下拉菜单

### 6.3 页面设计

#### 登录页 (LoginView — `/login`)

- 居中登录表单
- 用户名 + 密码输入
- 登录成功 → 跳转 `/dashboard`

#### 数据看板 (DashboardView — `/dashboard`)

| 指标卡片 | 说明 |
|----------|------|
| 今日订单 | 今日新增订单数量 |
| 今日营收 | 今日订单总金额 |
| 待处理订单 | 状态为 PENDING 的订单数 |
| 总营收 | 历史订单总金额 |

#### 分类管理 (CategoryView — `/category`)

| 功能 | 说明 |
|------|------|
| 分类列表 | 表格展示全部分类（名称、图标、排序、状态、操作） |
| 新增分类 | Modal 表单：名称(必填)、图标、排序 |
| 编辑分类 | Modal 表单：同新增，额外可修改状态 |
| 删除分类 | 确认后删除 |
| 状态展示 | `StatusTag` 组件，启用=绿色，禁用=红色 |

#### 商品管理 (ProductView — `/product`)

| 功能 | 说明 |
|------|------|
| 筛选栏 | 分类下拉 + 关键词搜索 |
| 商品列表 | 分页表格（名称、分类、价格、描述、状态、操作） |
| 新增商品 | Modal 表单：名称、分类(下拉)、价格、图片URL、描述、排序 |
| 编辑商品 | Modal 表单：同新增，额外可修改状态 |
| 上下架 | `ProductStatusSwitch` 内联开关组件 |
| 删除商品 | 确认后删除 |

#### 订单管理 (OrderView — `/order`)

| 功能 | 说明 |
|------|------|
| 状态筛选 | Radio 按钮组：全部/待制作/制作中/已完成/已取消 |
| 订单列表 | 分页表格（订单号、用户、金额、状态、时间、操作） |
| 订单商品 | Popover 展示订单明细 |
| 状态更新 | `OrderStatusSelect` 下拉修改订单状态 |

### 6.4 状态管理设计

#### Auth Store (`stores/auth.ts`)

```
State:
  user: UserInfo | null
  token: string | null
  isLoggedIn: computed   // !!token
  isAdmin: computed      // user.role === 'ADMIN'

Actions:
  login(username, password)
  logout()
  setUser(userInfo)
```

#### App Store (`stores/app.ts`)

```
State:
  collapsed: boolean     // 侧边栏折叠状态

Actions:
  toggleCollapsed()
```

---

## 7. 认证与安全

### 7.1 JWT 认证流程

```
1. 用户登录 → 服务器验证密码
2. 生成 accessToken (15min) + refreshToken (7d)
3. 客户端存储 accessToken + refreshToken 到 localStorage
4. 每次请求携带 Authorization: Bearer <accessToken>
5. JwtAuthenticationFilter 校验 Token → 设置 SecurityContext
6. Token 过期 → 用 refreshToken 换取新 accessToken
7. Refresh 失败 → 清除 Token → 跳转登录页
```

### 7.2 端点权限

| 端点 | 认证 | 角色 |
|------|------|------|
| `POST /api/auth/**` | 否 | 公开 |
| `GET /api/products/**` | 否 | 公开 |
| `GET /api/categories/**` | 否 | 公开 |
| `POST /api/orders` | 是 | CUSTOMER / ADMIN |
| `GET /api/orders/mine` | 是 | CUSTOMER / ADMIN |
| `GET /api/orders` | 是 | ADMIN |
| `PATCH /api/orders/{id}/status` | 是 | ADMIN |
| `GET /api/orders/dashboard/stats` | 是 | ADMIN |
| `POST/PUT/DELETE /api/categories` | 是 | ADMIN |
| `POST/PUT/DELETE /api/products` | 是 | ADMIN |

### 7.3 密码安全

- 使用 BCrypt 加密存储，不存储明文
- 前端传输走 HTTPS（生产环境）

---

## 8. 统一响应与异常处理

### 8.1 响应格式

所有 API 返回统一的 `Result<T>` 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 8.2 错误码

| 错误码 | 含义 | 场景 |
|--------|------|------|
| 200 | 操作成功 | 查询/更新成功 |
| 201 | 创建成功 | 新增资源 |
| 400 | 请求参数错误 | 校验失败 |
| 401 | 未认证 | Token 缺失/无效 |
| 403 | 无权限 | 权限不足 |
| 404 | 资源不存在 | 查询无结果 |
| 500 | 服务器内部错误 | 未预期异常 |
| 1001 | 用户名已存在 | 注册时用户名冲突 |
| 1002 | 用户名或密码错误 | 登录失败 |
| 1003 | Token无效或已过期 | Token 校验失败 |
| 2001 | 商品已下架 | 下单时商品状态异常 |
| 3001 | 订单状态不正确 | 状态流转校验失败 |

### 8.3 异常处理

```
GlobalExceptionHandler (@RestControllerAdvice):
  BusinessException          → Result(code, message)
  MethodArgumentNotValidException → Result(400, 字段错误信息)
  BadCredentialsException    → Result(401, "认证失败")
  AccessDeniedException      → Result(403, "无权限")
  Exception                  → Result(500, "服务器内部错误")
```

---

## 9. API 接口设计

### 9.1 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/refresh` | 刷新令牌 | 否 |
| POST | `/api/auth/logout` | 用户登出 | 是 |

### 9.2 分类模块 `/api/categories`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/categories` | 获取全部分类 | 否 |
| POST | `/api/categories` | 创建分类 | Admin |
| PUT | `/api/categories/{id}` | 更新分类 | Admin |
| DELETE | `/api/categories/{id}` | 删除分类 | Admin |

### 9.3 商品模块 `/api/products`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/products` | 分页查询商品 | 否 |
| GET | `/api/products/list` | 按分类查询列表 | 否 |
| GET | `/api/products/{id}` | 获取商品详情 | 否 |
| POST | `/api/products` | 创建商品 | Admin |
| PUT | `/api/products/{id}` | 更新商品 | Admin |
| PATCH | `/api/products/{id}/status` | 上下架 | Admin |
| DELETE | `/api/products/{id}` | 删除商品 | Admin |

**GET /api/products 查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 10 |
| categoryId | long | 否 | 分类 ID 筛选 |
| keyword | string | 否 | 关键词搜索（名称模糊） |
| status | int | 否 | 状态筛选（0/1） |

### 9.4 订单模块 `/api/orders`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/orders` | 创建订单 | 是 |
| GET | `/api/orders/mine` | 我的订单 | 是 |
| GET | `/api/orders` | 全部订单 | Admin |
| GET | `/api/orders/{id}` | 订单详情 | 是 |
| PATCH | `/api/orders/{id}/status` | 更新状态 | Admin |
| GET | `/api/orders/dashboard/stats` | 数据统计 | Admin |

**GET /api/orders / /api/orders/mine 查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 10 |
| status | string | 否 | PENDING/MAKING/DONE/CANCELLED |

---

## 10. 部署说明

### 10.1 开发环境

```bash
# 1. 启动后端
cd admin && ./mvnw spring-boot:run

# 2. 启动客户端（新终端）
cd client && bun install && bun run dev    # http://localhost:5174

# 3. 启动管理后台（新终端）
cd admin-front && bun install && bun run dev  # http://localhost:5173
```

### 10.2 生产构建

```bash
# 后端
cd admin && ./mvnw clean package -DskipTests
java -jar target/zentea-*.jar

# 客户端
cd client && bun run build    # 输出到 dist/

# 管理后台
cd admin-front && bun run build  # 输出到 dist/
```

### 10.3 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `ZENTEA_DB_PATH` | SQLite 数据库文件路径 | `./zentea.db` |
| `JWT_SECRET` | JWT 签名密钥 | 配置文件内 |
| `SERVER_PORT` | 后端服务端口 | 8080 |
