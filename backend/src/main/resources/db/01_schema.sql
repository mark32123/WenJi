-- 创建数据库
CREATE DATABASE IF NOT EXISTS wenji DEFAULT CHARACTER SET UTF-8 COLLATE UTF-8;

USE wenji;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '常驻之地',
    `gender` INT DEFAULT 2 COMMENT '性别 1:男 0:女 2:保密',
    `experience` INT DEFAULT 0 COMMENT '阅历值',
    `level` VARCHAR(20) DEFAULT '见习学徒' COMMENT '等级',
    `status` VARCHAR(10) DEFAULT '1' COMMENT '状态 0:禁用 1:正常',
    `is_real_name_verified` INT DEFAULT 0 COMMENT '是否实名认证 0:未认证 1:已认证',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 遗产景点表
CREATE TABLE IF NOT EXISTS `heritage_sites` (
    `site_id` VARCHAR(20) NOT NULL COMMENT '站点ID',
    `site_code` VARCHAR(20) DEFAULT NULL COMMENT '站点编码',
    `name` VARCHAR(100) NOT NULL COMMENT '站点名称',
    `en_name` VARCHAR(100) DEFAULT NULL COMMENT '站点英文名称',
    `type` INT DEFAULT NULL COMMENT '站点类型',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `level` VARCHAR(20) DEFAULT NULL COMMENT '级别',
    `province_code` VARCHAR(10) DEFAULT NULL COMMENT '省份编码',
    `city_code` VARCHAR(10) DEFAULT NULL COMMENT '市编码',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    `location_point` POINT NOT NULL SRID 4326 COMMENT '空间位置',
    `geohash` VARCHAR(20) DEFAULT NULL COMMENT 'Geohash编码',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `description` TEXT COMMENT '描述',
    `history` TEXT COMMENT '历史',
    `techniques` TEXT COMMENT '技艺',
    `best_season` VARCHAR(50) DEFAULT NULL COMMENT '最佳季节',
    `suitable_duration` INT DEFAULT NULL COMMENT '建议游览时长(小时)',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `official_url` VARCHAR(255) DEFAULT NULL COMMENT '官方网站',
    `status` INT DEFAULT 1 COMMENT '状态 1-正常 0-关闭',
    `is_recommended` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    `popularity` DECIMAL(10, 2) DEFAULT 0 COMMENT '人气值',
    `visit_count` INT DEFAULT 0 COMMENT '访问次数',
    `rating` DECIMAL(3, 2) DEFAULT NULL COMMENT '评分',
    `capacity` INT DEFAULT NULL COMMENT '容量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`site_id`),
    SPATIAL KEY `idx_location` (`location_point`),
    KEY `idx_city_code` (`city_code`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='遗产景点表';

-- 景点图片表
CREATE TABLE IF NOT EXISTS `site_images` (
    `image_id` BIGINT NOT NULL AUTO_INCREMENT,
    `site_id` VARCHAR(20) NOT NULL,
    `image_url` VARCHAR(255) NOT NULL,
    `image_type` VARCHAR(20) DEFAULT 'photo',
    `sort_order` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`image_id`),
    KEY `idx_site_id` (`site_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点图片表';

-- 景点开放时间表
CREATE TABLE IF NOT EXISTS `opening_hours` (
    `hour_id` BIGINT NOT NULL AUTO_INCREMENT,
    `site_id` VARCHAR(20) NOT NULL,
    `day_of_week` INT NOT NULL COMMENT '0-6 周日到周六',
    `open_time` TIME DEFAULT NULL,
    `close_time` TIME DEFAULT NULL,
    `is_closed` TINYINT(1) DEFAULT 0,
    PRIMARY KEY (`hour_id`),
    KEY `idx_site_id` (`site_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放时间表';

-- AI聊天会话表
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `session_id` VARCHAR(50) NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '会话标题',
    `type` VARCHAR(20) DEFAULT 'chat' COMMENT '会话类型',
    `current_location` VARCHAR(100) DEFAULT NULL COMMENT '当前位置',
    `session_context` TEXT DEFAULT NULL COMMENT '会话上下文',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`session_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

-- AI聊天消息表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` VARCHAR(50) NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色 user/assistant',
    `content` TEXT COMMENT '消息内容',
    `message_type` VARCHAR(20) DEFAULT 'text' COMMENT '消息类型',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`message_id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';

-- 旅行笔记表
CREATE TABLE IF NOT EXISTS `travel_blog` (
    `blog_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `site_id` VARCHAR(50) DEFAULT NULL COMMENT '景点 ID',
    `title` VARCHAR(100) NOT NULL,
    `content` TEXT,
    `cover_image` VARCHAR(255) DEFAULT NULL,
    `location` VARCHAR(100) DEFAULT NULL,
    `latitude` DECIMAL(10, 7) DEFAULT NULL,
    `longitude` DECIMAL(10, 7) DEFAULT NULL,
    `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片列表 JSON 字符串',
    `experience_earned` INT DEFAULT 0 COMMENT '获得的经验值',
    `view_count` INT DEFAULT 0,
    `like_count` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`blog_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_site_id` (`site_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅行笔记表';

-- 用户徽章表
CREATE TABLE IF NOT EXISTS `user_badge` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `badge_id` VARCHAR(50) NOT NULL,
    `badge_name` VARCHAR(100) NOT NULL,
    `badge_image` VARCHAR(255) DEFAULT NULL,
    `acquired_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户徽章表';

-- 徽章定义表
CREATE TABLE IF NOT EXISTS `badge` (
    `badge_id` INT NOT NULL AUTO_INCREMENT COMMENT '徽章 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '徽章名称',
    `description` VARCHAR(255) COMMENT '徽章描述',
    `icon_url` VARCHAR(255) COMMENT '徽章图标 URL',
    `type` VARCHAR(50) NOT NULL COMMENT '徽章类型 blog/footprint 等',
    `threshold` INT NOT NULL COMMENT '达成条件阈值',
    `experience_bonus` INT DEFAULT 0 COMMENT '经验奖励',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`badge_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='徽章定义表';

-- 积分流水表
CREATE TABLE IF NOT EXISTS `point_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `type` TINYINT NOT NULL COMMENT '类型：1-获得，2-消耗',
    `source` VARCHAR(50) NOT NULL COMMENT '来源：blog/badge/mall_exchange',
    `experience` INT NOT NULL COMMENT '变动阅历值',
    `description` VARCHAR(200) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`record_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- 区域表
CREATE TABLE IF NOT EXISTS `regions` (
    `region_code` VARCHAR(20) NOT NULL COMMENT '区域编码',
    `parent_code` VARCHAR(20) DEFAULT NULL COMMENT '父级编码',
    `name` VARCHAR(100) NOT NULL COMMENT '区域名称',
    `level` INT NOT NULL COMMENT '层级 1-省 2-市 3-区',
    `center_lat` DECIMAL(10, 7) DEFAULT NULL COMMENT '中心纬度',
    `center_lng` DECIMAL(10, 7) DEFAULT NULL COMMENT '中心经度',
    `polygon` TEXT DEFAULT NULL COMMENT '边界多边形',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`region_code`),
    KEY `idx_parent_code` (`parent_code`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';
