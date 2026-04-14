-- User table
CREATE TABLE IF NOT EXISTS user (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(50),
    phone       VARCHAR(20),
    avatar_url  VARCHAR(500),
    role        VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    status      INTEGER      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refresh token table
CREATE TABLE IF NOT EXISTS refresh_token (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER      NOT NULL,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expire_time DATETIME     NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Category table
CREATE TABLE IF NOT EXISTS category (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        VARCHAR(50)  NOT NULL,
    icon        VARCHAR(500),
    sort        INTEGER      NOT NULL DEFAULT 0,
    status      INTEGER      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product table
CREATE TABLE IF NOT EXISTS product (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id  INTEGER       NOT NULL,
    name         VARCHAR(100)  NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    image_url    VARCHAR(500),
    description  TEXT,
    status       INTEGER       NOT NULL DEFAULT 1,
    sort         INTEGER       NOT NULL DEFAULT 0,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no     VARCHAR(32)    NOT NULL UNIQUE,
    user_id      INTEGER        NOT NULL,
    total_amount DECIMAL(10,2)  NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    remark       TEXT,
    create_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Order item table
CREATE TABLE IF NOT EXISTS order_item (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id     INTEGER        NOT NULL,
    product_id   INTEGER        NOT NULL,
    product_name VARCHAR(100)   NOT NULL,
    quantity     INTEGER        NOT NULL DEFAULT 1,
    price        DECIMAL(10,2)  NOT NULL,
    specs        TEXT,
    subtotal     DECIMAL(10,2)  NOT NULL,
    create_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_product_category_id ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_create_time ON orders(create_time);
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);
CREATE INDEX IF NOT EXISTS idx_user_username ON user(username);
