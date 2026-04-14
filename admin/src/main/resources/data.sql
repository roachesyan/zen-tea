-- Admin account (password: admin123, BCrypt encoded)
INSERT INTO user (username, password, nickname, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'ADMIN', 1);

-- Default categories
INSERT INTO category (name, icon, sort, status) VALUES ('经典奶茶', '🧋', 1, 1);
INSERT INTO category (name, icon, sort, status) VALUES ('水果茶', '🍋', 2, 1);
INSERT INTO category (name, icon, sort, status) VALUES ('奶盖茶', '🥛', 3, 1);
INSERT INTO category (name, icon, sort, status) VALUES ('纯茶', '🍵', 4, 1);
INSERT INTO category (name, icon, sort, status) VALUES ('冰沙', '🧊', 5, 1);
INSERT INTO category (name, icon, sort, status) VALUES ('小料加购', '🫘', 6, 1);

-- Products for 经典奶茶 (category_id = 1)
INSERT INTO product (category_id, name, price, description, status, sort) VALUES
(1, '珍珠奶茶', 12.00, '经典珍珠奶茶，Q弹珍珠配上醇厚奶茶', 1, 1),
(1, '波霸奶茶', 14.00, '大颗波霸，嚼劲十足', 1, 2),
(1, '红豆奶茶', 13.00, '绵密红豆与丝滑奶茶的完美结合', 1, 3),
(1, '芋泥奶茶', 16.00, '手作芋泥，浓郁香醇', 1, 4);

-- Products for 水果茶 (category_id = 2)
INSERT INTO product (category_id, name, price, description, status, sort) VALUES
(2, '满杯百香果', 15.00, '新鲜百香果，酸甜可口', 1, 1),
(2, '超级柠檬茶', 12.00, '手锤柠檬，清爽解渴', 1, 2),
(2, '芒果冰沙', 16.00, '新鲜芒果制作，热带风情', 1, 3),
(2, '西瓜汁', 10.00, '鲜榨西瓜汁，夏日必备', 1, 4);

-- Products for 奶盖茶 (category_id = 3)
INSERT INTO product (category_id, name, price, description, status, sort) VALUES
(3, '芝士奶盖绿茶', 16.00, '浓郁芝士奶盖搭配清香绿茶', 1, 1),
(3, '芝士奶盖红茶', 16.00, '经典红茶底，浓厚奶盖', 1, 2),
(3, '芝士草莓', 18.00, '新鲜草莓与芝士奶盖', 1, 3);

-- Products for 纯茶 (category_id = 4)
INSERT INTO product (category_id, name, price, description, status, sort) VALUES
(4, '茉莉花茶', 8.00, '清香茉莉，回味悠长', 1, 1),
(4, '四季春茶', 8.00, '清雅四季春，口感清新', 1, 2),
(4, '铁观音', 10.00, '传统铁观音，茶香四溢', 1, 3);

-- Products for 冰沙 (category_id = 5)
INSERT INTO product (category_id, name, price, description, status, sort) VALUES
(5, '芒果冰沙', 18.00, '芒果冰沙，夏日清凉', 1, 1),
(5, '草莓冰沙', 18.00, '草莓冰沙，酸甜可口', 1, 2);

-- Products for 小料加购 (category_id = 6)
INSERT INTO product (category_id, name, price, description, status, sort) VALUES
(6, '珍珠', 3.00, 'Q弹珍珠', 1, 1),
(6, '椰果', 3.00, '爽脆椰果', 1, 2),
(6, '芋圆', 4.00, '软糯芋圆', 1, 3),
(6, '红豆', 3.00, '绵密红豆', 1, 4);
