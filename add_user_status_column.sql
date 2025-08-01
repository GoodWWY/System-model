-- 为用户表添加status字段（如果不存在）
ALTER TABLE `user` 
ADD COLUMN IF NOT EXISTS `status` varchar(20) DEFAULT 'active' COMMENT '用户状态：active-正常，inactive-禁用' AFTER `bio`;

-- 更新现有用户的状态为active（如果状态为空）
UPDATE `user` SET `status` = 'active' WHERE `status` IS NULL OR `status` = '';

-- 创建状态字段的索引
ALTER TABLE `user` ADD INDEX IF NOT EXISTS `idx_status` (`status`); 