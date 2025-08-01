-- 创建用户活动日志表
CREATE TABLE IF NOT EXISTS `user_activities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `action_type` varchar(50) NOT NULL COMMENT '操作类型',
  `action_description` varchar(255) NOT NULL COMMENT '操作描述',
  `target_type` varchar(50) DEFAULT NULL COMMENT '目标类型(user, article, role等)',
  `target_id` varchar(100) DEFAULT NULL COMMENT '目标ID',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态(1:成功, 0:失败)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活动日志表';

-- 插入一些示例数据
INSERT INTO `user_activities` (`user_id`, `username`, `action_type`, `action_description`, `target_type`, `target_id`, `ip_address`, `status`, `created_at`) VALUES
(1, 'admin', 'LOGIN', '用户登录系统', 'user', '1', '127.0.0.1', 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(1, 'admin', 'UPDATE_PROFILE', '修改个人信息', 'user', '1', '127.0.0.1', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(1, 'admin', 'UPLOAD_AVATAR', '上传头像', 'user', '1', '127.0.0.1', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(1, 'admin', 'CREATE_USER', '创建新用户', 'user', '2', '127.0.0.1', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 'admin', 'UPDATE_ROLE', '修改用户角色', 'role', '1', '127.0.0.1', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'admin', 'SYSTEM_BACKUP', '执行系统备份', 'system', NULL, '127.0.0.1', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)); 