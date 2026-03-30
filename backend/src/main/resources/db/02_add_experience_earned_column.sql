-- 修复 travel_blog 表和 point_record 表，添加缺失的字段


-- ==================== travel_blog 表修复 ====================

-- 添加 site_id 字段
ALTER TABLE `travel_blog` 
ADD COLUMN `site_id` VARCHAR(50) DEFAULT NULL COMMENT '景点 ID' AFTER `user_id`;

-- 添加 experience_earned 字段
ALTER TABLE `travel_blog` 
ADD COLUMN `experience_earned` INT DEFAULT 0 COMMENT '获得的经验值' AFTER `images`;

-- 添加 images 字段（如果不存在）
ALTER TABLE `travel_blog` 
ADD COLUMN `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片列表 JSON 字符串' AFTER `longitude`;

-- 添加 site_id 索引
ALTER TABLE `travel_blog` 
ADD INDEX `idx_site_id` (`site_id`);

-- ==================== point_record 表修复 ====================

-- 检查 point_record 表是否存在，不存在则创建
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
