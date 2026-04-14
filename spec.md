# 奶茶点单系统项目规格说明书 (Project Specification)

## 1. 项目概述 (Project Overview)
本项目是一个轻量级的奶茶在线点单与后台管理系统。系统旨在为普通用户提供便捷的移动端点单、支付体验，同时为商家提供高效的订单处理与商品管理能力。

项目采用前后端分离架构，拆分为三个独立子工程：**移动端（Client）**、**后端接口（Admin/Backend）**、**后台管理前端（Admin-Front）**。

## 2. 系统架构与技术栈 (Architecture & Tech Stack)

### 2.1 技术栈总览
| 模块 | 核心技术 | UI 框架/数据库 | 语言 |
| :--- | :--- | :--- | :--- |
| **移动端 (client)** | Vue 3 + Vite + Pinia + Vue Router + Bun | Vant UI | TypeScript |
| **后台前端 (admin-front)** | Vue 3 + Vite + Pinia + Vue Router + Bun | Ant Design Vue | TypeScript |
| **后端服务 (admin)** | Spring Boot 3.x | SQLite + MyBatis-Plus | Java |

---

## 3. 模块详细设计 (Module Specifications)

### 3.1 移动端 - Client (面向普通用户)
**目标设备：** 手机端 (Mobile-First)
**核心功能：** 浏览菜单、选购、下单付款、订单状态查看。

* **页面路由规划：**
    * `/home`：首页（展示推荐茶饮、轮播图）。
    * `/menu`：菜单页（左侧分类导航，右侧茶饮列表，支持加入购物车）。
    * `/cart`：购物车页（修改数量、计算总价）。
    * `/checkout`：结算页（确认订单信息、模拟支付）。
    * `/order`：订单列表页（查看历史订单及当前订单状态）。
* **组件与交互说明：**
    * 使用 `van-sidebar` 和 `van-list` 实现菜单的分类联动。
    * 商品规格（如：冰块、甜度、加料）使用 `van-sku` 或自定义弹窗实现。
    * 状态管理使用 Pinia 持久化购物车数据。

### 3.2 后端服务 - Admin (服务端 API)
**目标：** 提供稳定的 RESTful API 接口，处理业务逻辑与数据持久化。
**核心功能：** 基础数据增删改查、业务流转。

* **数据库设计 (SQLite) 核心表结构预览：**
    1.  `category` (茶饮分类表)：`id`, `name`, `sort`, `create_time`
    2.  `product` (茶饮商品表)：`id`, `category_id`, `name`, `price`, `image_url`, `status` (上下架), `description`
    3.  `orders` (订单主表)：`id`, `order_no`, `total_amount`, `status` (待支付/制作中/已完成), `create_time`
    4.  `order_item` (订单明细表)：`id`, `order_id`, `product_id`, `quantity`, `price`, `specs` (规格快照)
* **MyBatis-Plus 规范：**
    * 利用 MyBatis-Plus 提供的 `IService` 和 `BaseMapper` 实现基础 CRUD，减少重复 SQL 编写。
    * 配置分页插件 `PaginationInnerInterceptor` 以支持后台管理系统的数据表格分页。
    * 统一封装全局响应对象 `Result<T>`（包含 `code`, `message`, `data`）和全局异常处理 `@RestControllerAdvice`。

### 3.3 后台管理前端 - Admin-Front (面向商家/管理员)
**目标设备：** PC/桌面端
**核心功能：** 订单接单与状态流转、商品库维护、分类管理。

* **页面布局：** 经典的中后台布局（侧边栏菜单 + 顶部 Header + 面包屑导航 + 内容区）。
* **页面路由规划：**
    * `/dashboard`：数据看板（今日订单数、营业额概览）。
    * `/category`：茶饮分类管理（支持增删改查、拖拽排序）。
    * `/product`：茶饮商品管理（列表展示、上/下架切换、富文本编辑或图片上传）。
    * `/order`：订单管理（实时刷新待处理订单，修改订单状态为“制作中”或“已出餐”）。
* **组件与交互说明：**
    * 重度依赖 Ant Design Vue 的 `a-table` (数据表格，配合分页)、`a-form` (表单校验)、`a-modal` (弹窗操作)。
    * 网络请求统一使用 Axios 封装，并配置请求/响应拦截器以处理统一的错误提示和 Token（若后续加入登录鉴权）。

---

## 4. 开发与部署规范 (Development & Deployment)

* **接口规范**：前后端统一遵循 RESTful 风格，如 `GET /api/products` 获取列表，`POST /api/orders` 创建订单。
* **数据库初始化**：鉴于使用了 SQLite，项目根目录需保留一份初始化的 `schema.sql`，Spring Boot 启动时若检测到 `.db` 文件不存在，则自动初始化表结构，方便开箱即用。
* **代码规范**：前端启用 ESLint + Prettier，后端遵循 Alibaba Java 开发手册。

---

## 5. 项目质量要求 (Project Quality Requirements)

### 5.1 后端质量要求

#### 项目结构与分层
* 严格遵循 **Controller → Service → DAO/Mapper** 三层架构，禁止跨层调用。
* Controller 仅负责参数校验与响应封装，业务逻辑全部下沉到 Service 层。

#### RESTful API 设计
* 资源命名使用复数名词（`/api/products`、`/api/orders`）。
* 正确使用 HTTP 方法（GET 查询、POST 创建、PUT 全量更新、PATCH 部分更新、DELETE 删除）。
* 合理使用 HTTP 状态码（200 成功、201 创建、400 参数错误、401 未认证、403 无权限、404 不存在、500 服务器错误）。
* 需提供 API 文档（Swagger/OpenAPI 或独立文档）。

#### 身份认证与授权
* 使用 **JWT** 实现用户认证，Token 携带在请求头 `Authorization: Bearer <token>`。
* 区分角色权限：普通用户（下单）、管理员（后台管理）。
* 实现注册、登录、Token 校验、Token 刷新接口。

#### 异常处理
* 自定义业务异常类（如 `BusinessException`），携带错误码与错误信息。
* 全局异常拦截 `@RestControllerAdvice`，统一返回 `Result<T>` 格式。
* 区分参数校验异常、业务异常、系统异常，分别返回不同状态码。

#### 日志记录
* 使用 **SLF4J + Logback**，禁止使用 `System.out.println`。
* 日志级别规范：ERROR（系统异常）、WARN（业务警告）、INFO（关键操作，如创建订单）、DEBUG（调试信息）。
* 关键业务操作需记录审计日志（谁、什么时候、做了什么、结果如何）。

#### 测试
* 使用 **JUnit 5 + Mockito** 编写单元测试与集成测试。
* Service 层核心业务逻辑需 Mock DAO 层进行单元测试。
* Controller 层使用 `@SpringBootTest` + `MockMvc` 进行集成测试。
* 测试覆盖率目标 **≥ 80%**。

#### 数据库
* 复杂查询需提供 SQL 执行计划分析，避免全表扫描。
* 联表查询使用 MyBatis-Plus 的 LambdaQueryWrapper 或 XML 映射。
* 索引设计需覆盖高频查询字段。

#### 事务管理
* Service 层涉及多表操作的方法使用 `@Transactional`。
* 明确事务传播行为（默认 `REQUIRED`）与回滚规则（对 `Exception` 类异常回滚）。
* 只读操作标注 `@Transactional(readOnly = true)` 优化性能。

#### 文档
* 需提供详细设计文档（模块设计、接口设计、数据库表结构设计）。
* 数据库表结构文档包含字段说明、索引、外键约束、表关系 ER 图。

### 5.2 前端质量要求

#### 用户任务思维
* 从用户操作路径定义页面结构与状态流转，而非仅关注接口调用。
* 每个交互需说清楚：**用户为什么点 → 点完看到什么 → 失败怎么办**。

#### 页面实现
* 能独立完成列表、表单、详情、弹窗、筛选等常见页面类型。
* 页面需真实可用，不允许 demo 级拼装（空数据、硬编码、不可交互的不算完成）。

#### 前端状态管理
* 明确区分四类状态：**本地状态**（组件内）、**派生状态**（计算得出）、**异步状态**（请求中/成功/失败）、**全局状态**（Pinia Store）。
* 避免状态冗余和双向污染，单一数据源原则。
* 每个涉及异步请求的页面必须处理 **loading / empty / error / disabled** 四种必备状态。

#### 组件边界设计
* 按职责拆分组件：**展示组件**（纯 UI）、**容器组件**（逻辑与数据）、**通用组件**（可复用）。
* Props、事件、类型定义需合理，保证组件可复用可维护。
* 禁止一个页面写到底，单组件代码量不超过 **300 行**。

#### 异步交互与接口联动
* 正确处理请求生命周期：发起、取消（组件卸载时）、并发、重试、错误提示。
* 解决筛选、分页、条件切换导致的状态一致性问题。
* 避免重复请求、旧数据覆盖新数据（竞态条件）。

#### 页面体验
* 处理空态（无数据提示）、异常态（错误提示与重试）、权限态（无权限引导）、骨架屏（加载占位）、禁用态（按钮/表单不可操作）。
* 关注可读性、操作反馈（Toast/通知）、信息层级。
* 目标：**用户能完成任务，而非仅仅看到数据**。

#### 样式与布局
* 完成常见布局（Flex/Grid）、响应式适配、间距层级、对齐规范。
* 遵循所选 UI 框架（Vant / Ant Design Vue）的设计规范。
* 页面视觉需达到业务可交付标准，不允许"功能能用、视觉不可交付"。

#### 前端质量保障
* 进行组件级、页面级、交互级自测。
* 关键流程补充单元测试或集成测试。
* 提交前需验证页面逻辑、状态流转和交互行为正确。

#### 前端闭环交付
* 每个功能模块需完成从页面实现 → 联调 → 自测 → 修复 → 交付的完整闭环。
* 交付物可追溯：设计理解 → 实现说明 → 测试记录 → 问题修复记录。