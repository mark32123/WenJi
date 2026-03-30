-- 最终修复脚本 - 只创建缺失的表
-- 执行时间：2025-12-18

-- ==================== badge 表（必需） ====================

DROP TABLE IF EXISTS `badge`;

CREATE TABLE `badge` (
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

-- 插入初始数据
INSERT INTO `badge` (`name`, `description`, `type`, `threshold`, `experience_bonus`) VALUES 
('初试啼声', '发布第一篇旅游心得', 'blog', 1, 50),
('足迹遍布', '在 5 个不同的景点打卡', 'footprint', 5, 200),
('资深驴友', '累计发布 10 篇旅游心得', 'blog', 10, 500),
('文化传承者', '在非遗类景点打卡', 'footprint', 1, 100);

-- ==================== point_record 表（必需） ====================

DROP TABLE IF EXISTS `point_record`;

CREATE TABLE `point_record` (
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
