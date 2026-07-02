CREATE DATABASE IF NOT EXISTS youran_bookkeeping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE youran_bookkeeping;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50) NOT NULL UNIQUE,
  password    VARCHAR(255) NOT NULL,
  nickname    VARCHAR(50),
  email       VARCHAR(100),
  avatar      VARCHAR(255),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表
CREATE TABLE IF NOT EXISTS category (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(50) NOT NULL,
  icon        VARCHAR(50),
  parent_id   BIGINT DEFAULT NULL COMMENT 'NULL=一级分类, 非NULL=二级分类',
  sort_order  INT DEFAULT 0 COMMENT '排序号',
  user_id     BIGINT DEFAULT NULL COMMENT 'NULL=系统预设, 非NULL=用户自定义',
  is_deleted  TINYINT(1) DEFAULT 0,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (parent_id) REFERENCES category(id),
  FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消费记录表
CREATE TABLE IF NOT EXISTS expense_record (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT NOT NULL,
  category_id   BIGINT NOT NULL,
  amount        DECIMAL(10, 2) NOT NULL COMMENT '人民币金额',
  record_date   DATE NOT NULL COMMENT '消费日期',
  note          VARCHAR(255) COMMENT '备注',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预算表
CREATE TABLE IF NOT EXISTS budget (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  category_id BIGINT COMMENT 'NULL=总预算',
  month       VARCHAR(7) NOT NULL COMMENT '如: 2026-07',
  amount      DECIMAL(10, 2) NOT NULL,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入系统预设一级分类
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('餐饮饮食', 'rice', NULL, 1, NULL),
('交通出行', 'car', NULL, 2, NULL),
('居住物业', 'home', NULL, 3, NULL),
('购物消费', 'shopping', NULL, 4, NULL),
('娱乐休闲', 'game', NULL, 5, NULL),
('医疗健康', 'hospital', NULL, 6, NULL),
('教育学习', 'book', NULL, 7, NULL),
('人情社交', 'users', NULL, 8, NULL),
('通讯网络', 'phone', NULL, 9, NULL),
('其他', 'more', NULL, 10, NULL);

-- 插入二级分类
SET @parent_id = (SELECT id FROM category WHERE name='餐饮饮食' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('早餐', 'coffee', @parent_id, 1, NULL),
('午餐', 'food', @parent_id, 2, NULL),
('晚餐', 'food', @parent_id, 3, NULL),
('外卖', 'takeaway', @parent_id, 4, NULL),
('水果', 'fruit', @parent_id, 5, NULL),
('零食', 'snacks', @parent_id, 6, NULL),
('饮品', 'drink', @parent_id, 7, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='交通出行' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('公交地铁', 'bus', @parent_id, 1, NULL),
('打车', 'taxi', @parent_id, 2, NULL),
('加油', 'gas', @parent_id, 3, NULL),
('停车', 'parking', @parent_id, 4, NULL),
('维修保养', 'maintenance', @parent_id, 5, NULL),
('过路费', 'toll', @parent_id, 6, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='居住物业' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('房租', 'rent', @parent_id, 1, NULL),
('水电费', 'water', @parent_id, 2, NULL),
('物业费', 'property', @parent_id, 3, NULL),
('暖气费', 'heating', @parent_id, 4, NULL),
('网费', 'wifi', @parent_id, 5, NULL),
('维修', 'repair', @parent_id, 6, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='购物消费' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('服装鞋包', 'clothes', @parent_id, 1, NULL),
('日用品', 'daily', @parent_id, 2, NULL),
('数码产品', 'digital', @parent_id, 3, NULL),
('美妆护肤', 'cosmetics', @parent_id, 4, NULL),
('家居用品', 'furniture', @parent_id, 5, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='娱乐休闲' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('电影演出', 'film', @parent_id, 1, NULL),
('游戏充值', 'gamepad', @parent_id, 2, NULL),
('运动健身', 'sport', @parent_id, 3, NULL),
('旅游出行', 'travel', @parent_id, 4, NULL),
('KTV', 'mic', @parent_id, 5, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='医疗健康' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('门诊挂号', 'clinic', @parent_id, 1, NULL),
('药品', 'medicine', @parent_id, 2, NULL),
('体检', 'checkup', @parent_id, 3, NULL),
('牙科', 'tooth', @parent_id, 4, NULL),
('住院', 'hospital-bed', @parent_id, 5, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='教育学习' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('书籍', 'book-open', @parent_id, 1, NULL),
('课程培训', 'course', @parent_id, 2, NULL),
('文具', 'stationery', @parent_id, 3, NULL),
('考试报名', 'exam', @parent_id, 4, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='人情社交' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('聚餐请客', 'dinner', @parent_id, 1, NULL),
('礼物', 'gift', @parent_id, 2, NULL),
('红包', 'redpacket', @parent_id, 3, NULL),
('捐款', 'donate', @parent_id, 4, NULL),
('随礼', 'ceremony', @parent_id, 5, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='通讯网络' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('话费', 'call', @parent_id, 1, NULL),
('流量', 'data', @parent_id, 2, NULL),
('宽带', 'broadband', @parent_id, 3, NULL);

SET @parent_id = (SELECT id FROM category WHERE name='其他' AND parent_id IS NULL);
INSERT INTO category (name, icon, parent_id, sort_order, user_id) VALUES
('其他支出', 'more', @parent_id, 1, NULL);
