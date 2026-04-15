# 禅茶 ZenTea — 数据库表结构设计文档

> 版本：1.0 | 更新日期：2026-04-15 | 数据库：SQLite

---

## 目录

1. [概述](#1-概述)
2. [ER 关系图](#2-er-关系图)
3. [表结构详细设计](#3-表结构详细设计)
4. [索引设计](#4-索引设计)
5. [初始化数据](#5-初始化数据)

---

## 1. 概述

### 1.1 基本信息

| 项目 | 值 |
|------|-----|
| 数据库类型 | SQLite |
| 存储文件 | `./zentea.db`（可通过 `ZENTEA_DB_PATH` 环境变量配置） |
| 字符编码 | UTF-8 |
| 主键策略 | INTEGER AUTOINCREMENT |
| 命名规范 | 表名小写，字段名 snake_case |
| 自动初始化 | 首次启动检测表不存在时，自动执行 `schema.sql` + `data.sql` |

### 1.2 数据表清单

| 表名 | 说明 | 记录量级 |
|------|------|----------|
| `user` | 用户账号 | 百级 |
| `refresh_token` | JWT 刷新令牌 | 千级 |
| `category` | 商品分类 | 十级 |
| `product` | 商品 | 百级 |
| `orders` | 订单主表 | 千级 |
| `order_item` | 订单明细 | 万级 |

---

## 2. ER 关系图

```
┌──────────┐ 1    N ┌───────────────┐
│   user   │───────→│ refresh_token │
│          │        └───────────────┘
│          │
│          │ 1    N ┌──────────┐
│          │───────→│  orders  │
│          │        └────┬─────┘
│          │             │ 1
│          │             │
│          │             │ N
│          │        ┌────▼─────────┐
│          │        │  order_item  │
│          │        └────▲─────────┘
│          │             │ N
└──────────┘             │
                         │ 1
┌──────────┐ N     1 ┌───┴──────┐
│ product  │─────────→│ category │
└────┬─────┘          └──────────┘
     │
     │ 1
     │
     │ N
┌────▼─────────┐
│  order_item  │
└──────────────┘

关系说明:
  user        1:N  refresh_token  (一个用户可有多个刷新令牌)
  user        1:N  orders         (一个用户可有多个订单)
  orders      1:N  order_item     (一个订单包含多个商品明细)
  category    1:N  product        (一个分类下有多个商品)
  product     1:N  order_item     (一个商品可出现在多个订单明细中)
```

---

## 3. 表结构详细设计

### 3.1 user — 用户表

> 存储系统用户信息，包括消费者和管理员。

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | INTEGER | PK, AUTOINCREMENT | - | 用户 ID |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | - | 用户名（登录账号） |
| `password` | VARCHAR(255) | NOT NULL | - | BCrypt 加密后的密码 |
| `nickname` | VARCHAR(50) | - | NULL | 昵称 |
| `phone` | VARCHAR(20) | - | NULL | 手机号 |
| `avatar_url` | VARCHAR(500) | - | NULL | 头像 URL |
| `role` | VARCHAR(20) | NOT NULL | `'CUSTOMER'` | 角色：`ADMIN` / `CUSTOMER` |
| `status` | INTEGER | NOT NULL | `1` | 状态：`1`=启用，`0`=禁用 |
| `create_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 更新时间 |

**业务规则：**
- `username` 全局唯一，注册时校验
- `password` 使用 BCrypt 加密，不可明文存储
- `role` 取值：`ADMIN`（管理员）、`CUSTOMER`（消费者）
- `status` 为 `0` 时禁止登录

**Java 实体映射：** `com.zentea.entity.User`

---

### 3.2 refresh_token — 刷新令牌表

> 存储 JWT 刷新令牌，支持令牌撤销和多设备管理。

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | INTEGER | PK, AUTOINCREMENT | - | 令牌 ID |
| `user_id` | INTEGER | NOT NULL, FK | - | 所属用户 ID |
| `token` | VARCHAR(500) | NOT NULL, UNIQUE | - | 刷新令牌值 |
| `expire_time` | DATETIME | NOT NULL | - | 过期时间 |
| `create_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 创建时间 |

**外键：**

| 外键字段 | 引用 | 删除策略 |
|----------|------|----------|
| `user_id` | `user(id)` | `ON DELETE CASCADE` |

**业务规则：**
- 用户登出时删除该用户所有 refresh_token 记录
- 刷新 Token 时校验 `expire_time` 未过期
- `token` 全局唯一

**Java 实体映射：** `com.zentea.entity.RefreshToken`

---

### 3.3 category — 分类表

> 商品分类，如"经典奶茶"、"水果茶"等。

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | INTEGER | PK, AUTOINCREMENT | - | 分类 ID |
| `name` | VARCHAR(50) | NOT NULL | - | 分类名称 |
| `icon` | VARCHAR(500) | - | NULL | 分类图标（Emoji 或图片 URL） |
| `sort` | INTEGER | NOT NULL | `0` | 排序序号（升序） |
| `status` | INTEGER | NOT NULL | `1` | 状态：`1`=启用，`0`=禁用 |
| `create_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 更新时间 |

**业务规则：**
- `sort` 值越小越靠前
- `status` 为 `0` 时，客户端不展示该分类及其下商品
- 删除分类前需确认该分类下无商品，或级联处理

**Java 实体映射：** `com.zentea.entity.Category`

---

### 3.4 product — 商品表

> 商品信息，关联分类。

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | INTEGER | PK, AUTOINCREMENT | - | 商品 ID |
| `category_id` | INTEGER | NOT NULL, FK | - | 所属分类 ID |
| `name` | VARCHAR(100) | NOT NULL | - | 商品名称 |
| `price` | DECIMAL(10,2) | NOT NULL | - | 售价（元） |
| `image_url` | VARCHAR(500) | - | NULL | 商品图片 URL |
| `description` | TEXT | - | NULL | 商品描述 |
| `status` | INTEGER | NOT NULL | `1` | 状态：`1`=上架，`0`=下架 |
| `sort` | INTEGER | NOT NULL | `0` | 排序序号（升序） |
| `create_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 更新时间 |

**外键：**

| 外键字段 | 引用 | 删除策略 |
|----------|------|----------|
| `category_id` | `category(id)` | `RESTRICT`（默认） |

**业务规则：**
- `price` 最小值 0.01，精度两位小数
- `status` 为 `0` 时，客户端不展示，下单时校验不允许购买已下架商品
- `sort` 值越小越靠前
- 查询时通过 `category_id` 关联获取 `categoryName`

**Java 实体映射：** `com.zentea.entity.Product`

---

### 3.5 orders — 订单主表

> 订单头信息，记录用户下单的汇总数据。

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | INTEGER | PK, AUTOINCREMENT | - | 订单 ID |
| `order_no` | VARCHAR(32) | NOT NULL, UNIQUE | - | 订单编号 |
| `user_id` | INTEGER | NOT NULL, FK | - | 下单用户 ID |
| `total_amount` | DECIMAL(10,2) | NOT NULL | - | 订单总金额（元） |
| `status` | VARCHAR(20) | NOT NULL | `'PENDING'` | 订单状态 |
| `remark` | TEXT | - | NULL | 用户备注 |
| `create_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 更新时间 |

**外键：**

| 外键字段 | 引用 | 删除策略 |
|----------|------|----------|
| `user_id` | `user(id)` | `RESTRICT`（默认） |

**订单状态流转：**

```
PENDING (待制作)
  ├──→ MAKING (制作中)
  │      └──→ DONE (已完成)
  └──→ CANCELLED (已取消)
```

| 状态 | 值 | 说明 |
|------|-----|------|
| 待制作 | `PENDING` | 用户刚下单，等待商家确认 |
| 制作中 | `MAKING` | 商家已接单，正在制作 |
| 已完成 | `DONE` | 制作完成，已取餐 |
| 已取消 | `CANCELLED` | 订单已取消 |

**订单号规则：** `ZN` + `yyyyMMddHHmmss` + 4 位序列号，示例：`ZN202604151430250001`

**业务规则：**
- `order_no` 全局唯一，创建时由 `OrderNoGenerator` 生成
- `total_amount` = 所有 order_item 的 `subtotal` 之和
- 状态只允许按流转图单向推进
- 创建订单使用 `@Transactional`，同时写入 orders 和 order_item

**Java 实体映射：** `com.zentea.entity.Orders`

---

### 3.6 order_item — 订单明细表

> 订单中的商品明细，记录每个商品的下单信息。

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | INTEGER | PK, AUTOINCREMENT | - | 明细 ID |
| `order_id` | INTEGER | NOT NULL, FK | - | 所属订单 ID |
| `product_id` | INTEGER | NOT NULL | - | 商品 ID（下单时快照） |
| `product_name` | VARCHAR(100) | NOT NULL | - | 商品名称（下单时快照） |
| `quantity` | INTEGER | NOT NULL | `1` | 购买数量 |
| `price` | DECIMAL(10,2) | NOT NULL | - | 单价（下单时快照） |
| `specs` | TEXT | - | NULL | 规格信息（JSON 格式） |
| `subtotal` | DECIMAL(10,2) | NOT NULL | - | 小计 = price × quantity |
| `create_time` | DATETIME | NOT NULL | `CURRENT_TIMESTAMP` | 创建时间 |

**外键：**

| 外键字段 | 引用 | 删除策略 |
|----------|------|----------|
| `order_id` | `orders(id)` | `ON DELETE CASCADE` |

**specs 字段 JSON 格式：**

```json
{
  "ice": "normal",
  "sweetness": "half",
  "toppings": ["珍珠", "芋圆"]
}
```

| 字段 | 类型 | 可选值 | 说明 |
|------|------|--------|------|
| `ice` | string | `normal`, `less`, `no_ice`, `hot` | 冰度 |
| `sweetness` | string | `full`, `less`, `half`, `little`, `none` | 甜度 |
| `toppings` | string[] | 任意字符串数组 | 小料列表 |

**业务规则：**
- `product_name`、`price` 为下单时的快照，不随商品信息修改而变更
- `subtotal` = `price` × `quantity`
- 订单删除时级联删除明细（`ON DELETE CASCADE`）

**Java 实体映射：** `com.zentea.entity.OrderItem`

---

## 4. 索引设计

| 索引名 | 表 | 字段 | 类型 | 说明 |
|--------|-----|------|------|------|
| `idx_user_username` | `user` | `username` | UNIQUE | 用户名唯一索引，加速登录查询 |
| `idx_product_category_id` | `product` | `category_id` | NORMAL | 按分类查询商品 |
| `idx_product_status` | `product` | `status` | NORMAL | 按状态筛选商品 |
| `idx_orders_user_id` | `orders` | `user_id` | NORMAL | 查询用户订单 |
| `idx_orders_status` | `orders` | `status` | NORMAL | 按状态筛选订单 |
| `idx_orders_create_time` | `orders` | `create_time` | NORMAL | 按时间排序/统计 |
| `idx_order_item_order_id` | `order_item` | `order_id` | NORMAL | 查询订单明细 |

### 索引设计说明

- **user.username**：登录时高频查询，已声明 `UNIQUE`，自动创建索引
- **product.category_id**：菜单页按分类加载商品，最高频查询
- **product.status**：客户端只查询上架商品
- **orders.user_id**：用户查询"我的订单"
- **orders.status**：管理员按状态筛选订单
- **orders.create_time**：按时间倒序展示订单、统计报表
- **order_item.order_id**：加载订单明细

---

## 5. 初始化数据

### 5.1 管理员账号

| 字段 | 值 |
|------|-----|
| username | `admin` |
| password | `admin123`（BCrypt 加密存储） |
| nickname | 管理员 |
| role | `ADMIN` |
| status | 1（启用） |

### 5.2 商品分类

| ID | 名称 | 图标 | 排序 |
|----|------|------|------|
| 1 | 经典奶茶 | 🧋 | 1 |
| 2 | 水果茶 | 🍋 | 2 |
| 3 | 奶盖茶 | 🥛 | 3 |
| 4 | 纯茶 | 🍵 | 4 |
| 5 | 冰沙 | 🧊 | 5 |
| 6 | 小料加购 | 🫘 | 6 |

### 5.3 初始商品

#### 经典奶茶 (category_id = 1)

| 商品名 | 价格 | 描述 |
|--------|------|------|
| 珍珠奶茶 | ¥12.00 | 经典珍珠奶茶，Q弹珍珠配上醇厚奶茶 |
| 波霸奶茶 | ¥14.00 | 大颗波霸，嚼劲十足 |
| 红豆奶茶 | ¥13.00 | 绵密红豆与丝滑奶茶的完美结合 |
| 芋泥奶茶 | ¥16.00 | 手作芋泥，浓郁香醇 |

#### 水果茶 (category_id = 2)

| 商品名 | 价格 | 描述 |
|--------|------|------|
| 满杯百香果 | ¥15.00 | 新鲜百香果，酸甜可口 |
| 超级柠檬茶 | ¥12.00 | 手锤柠檬，清爽解渴 |
| 芒果冰沙 | ¥16.00 | 新鲜芒果制作，热带风情 |
| 西瓜汁 | ¥10.00 | 鲜榨西瓜汁，夏日必备 |

#### 奶盖茶 (category_id = 3)

| 商品名 | 价格 | 描述 |
|--------|------|------|
| 芝士奶盖绿茶 | ¥16.00 | 浓郁芝士奶盖搭配清香绿茶 |
| 芝士奶盖红茶 | ¥16.00 | 经典红茶底，浓厚奶盖 |
| 芝士草莓 | ¥18.00 | 新鲜草莓与芝士奶盖 |

#### 纯茶 (category_id = 4)

| 商品名 | 价格 | 描述 |
|--------|------|------|
| 茉莉花茶 | ¥8.00 | 清香茉莉，回味悠长 |
| 四季春茶 | ¥8.00 | 清雅四季春，口感清新 |
| 铁观音 | ¥10.00 | 传统铁观音，茶香四溢 |

#### 冰沙 (category_id = 5)

| 商品名 | 价格 | 描述 |
|--------|------|------|
| 芒果冰沙 | ¥18.00 | 芒果冰沙，夏日清凉 |
| 草莓冰沙 | ¥18.00 | 草莓冰沙，酸甜可口 |

#### 小料加购 (category_id = 6)

| 商品名 | 价格 | 描述 |
|--------|------|------|
| 珍珠 | ¥3.00 | Q弹珍珠 |
| 椰果 | ¥3.00 | 爽脆椰果 |
| 芋圆 | ¥4.00 | 软糯芋圆 |
| 红豆 | ¥3.00 | 绵密红豆 |
