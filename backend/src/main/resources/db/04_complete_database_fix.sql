-- 完整修复所有缺失的表结构和字段
-- 执行时间：2025-12-18

-- ==================== travel_blog 表修复 ====================


-- 添加 site_id 字段
ALTER TABLE `travel_blog` 
ADD COLUMN `site_id` VARCHAR(50) DEFAULT NULL COMMENT '景点 ID' AFTER `user_id`;

-- 添加 experience_earned 字段
ALTER TABLE `travel_blog` 
ADD COLUMN `experience_earned` INT DEFAULT 0 COMMENT '获得的经验值' AFTER `images`;

-- 添加 images 字段
ALTER TABLE `travel_blog` 
ADD COLUMN `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片列表 JSON 字符串' AFTER `longitude`;

-- 添加 site_id 索引
ALTER TABLE `travel_blog` 
ADD INDEX `idx_site_id` (`site_id`);

-- ==================== badge 表修复 ====================

-- 如果 badge 表不存在则创建
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

-- 插入初始徽章数据
INSERT INTO `badge` (`name`, `description`, `type`, `threshold`, `experience_bonus`) VALUES 
('初试啼声', '发布第一篇旅游心得', 'blog', 1, 50),
('足迹遍布', '在 5 个不同的景点打卡', 'footprint', 5, 200),
('资深驴友', '累计发布 10 篇旅游心得', 'blog', 10, 500),
('文化传承者', '在非遗类景点打卡', 'footprint', 1, 100)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ==================== point_record 表修复 ====================

-- 重建 point_record 表（确保结构正确）
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
