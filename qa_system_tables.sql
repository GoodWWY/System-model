-- 创建问答记录表
CREATE TABLE IF NOT EXISTS `qa_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '问答记录ID',
  `user_id` int(11) NOT NULL COMMENT '提问用户ID',
  `username` varchar(50) NOT NULL COMMENT '提问用户名',
  `question` text NOT NULL COMMENT '用户问题',
  `answer` longtext DEFAULT NULL COMMENT 'AI回答',
  `model_name` varchar(50) DEFAULT 'moonshot-v1-8k' COMMENT '使用的AI模型',
  `tokens_used` int(11) DEFAULT 0 COMMENT '消耗的token数量',
  `response_time` int(11) DEFAULT 0 COMMENT '响应时间(毫秒)',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending-处理中，completed-已完成，failed-失败',
  `error_message` text DEFAULT NULL COMMENT '错误信息',
  `rating` tinyint(1) DEFAULT NULL COMMENT '用户评分：1-5星',
  `feedback` text DEFAULT NULL COMMENT '用户反馈',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答记录表';

-- 创建问答分类表
CREATE TABLE IF NOT EXISTS `qa_categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(50) DEFAULT NULL COMMENT '分类图标',
  `color` varchar(20) DEFAULT NULL COMMENT '分类颜色',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `question_count` int(11) DEFAULT 0 COMMENT '问题数量',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答分类表';

-- 创建常见问题表
CREATE TABLE IF NOT EXISTS `qa_faqs` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  `category_id` int(11) DEFAULT NULL COMMENT '分类ID',
  `question` varchar(500) NOT NULL COMMENT '问题',
  `answer` text NOT NULL COMMENT '答案',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` int(11) DEFAULT 0 COMMENT '查看次数',
  `helpful_count` int(11) DEFAULT 0 COMMENT '有用次数',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `created_by` int(11) NOT NULL COMMENT '创建者ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_sort_order` (`sort_order`),
  FULLTEXT KEY `idx_question_answer` (`question`, `answer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常见问题表';

-- 插入默认分类数据
INSERT INTO `qa_categories` (`name`, `description`, `icon`, `color`, `sort_order`, `is_enabled`) VALUES
('技术问题', '编程、开发、技术相关问题', 'el-icon-cpu', '#409EFF', 1, 1),
('产品使用', '产品功能、使用方法相关问题', 'el-icon-guide', '#67C23A', 2, 1),
('系统问题', '系统故障、错误、性能相关问题', 'el-icon-warning', '#E6A23C', 3, 1),
('账户相关', '登录、注册、权限相关问题', 'el-icon-user', '#F56C6C', 4, 1),
('其他问题', '其他未分类的问题', 'el-icon-question', '#909399', 5, 1)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP;

-- 插入示例FAQ数据
INSERT INTO `qa_faqs` (`category_id`, `question`, `answer`, `tags`, `sort_order`, `is_enabled`, `created_by`) VALUES
(1, '如何使用Vue 3开发项目？', 'Vue 3是一个现代化的JavaScript框架...\n\n1. 安装Vue CLI\n2. 创建项目\n3. 编写组件\n4. 路由配置', 'Vue3,前端开发,JavaScript', 1, 1, 1),
(2, '如何重置密码？', '您可以通过以下步骤重置密码：\n\n1. 点击登录页面的"忘记密码"\n2. 输入注册邮箱\n3. 查收邮件并点击重置链接\n4. 设置新密码', '密码重置,账户安全', 2, 1, 1),
(3, '系统响应慢怎么办？', '如果遇到系统响应慢的问题，请尝试：\n\n1. 清除浏览器缓存\n2. 检查网络连接\n3. 刷新页面\n4. 联系技术支持', '性能问题,故障排查', 3, 1, 1),
(4, '如何修改个人资料？', '修改个人资料的步骤：\n\n1. 登录系统\n2. 点击右上角头像\n3. 选择"个人中心"\n4. 编辑相关信息并保存', '个人资料,用户设置', 4, 1, 1)
ON DUPLICATE KEY UPDATE
question = VALUES(question),
answer = VALUES(answer),
updated_at = CURRENT_TIMESTAMP; 