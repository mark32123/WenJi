-- 完整修复 point_record 表结构
-- 执行时间：2025-12-18

-- 如果 point_record 表存在，先删除（因为可能结构不对）
DROP TABLE IF EXISTS `point_record`;

-- 重新创建 point_record 表
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
