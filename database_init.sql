-- 创建系统设置表
CREATE TABLE IF NOT EXISTS `system_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `setting_key` varchar(100) NOT NULL COMMENT '设置键名',
  `setting_value` text COMMENT '设置值',
  `setting_type` varchar(50) NOT NULL DEFAULT 'custom' COMMENT '设置类型',
  `description` varchar(200) DEFAULT NULL COMMENT '设置描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`),
  KEY `idx_setting_type` (`setting_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 插入默认设置数据
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `setting_type`, `description`) VALUES
('system.name', 'Cursor Demo 管理系统', 'system', '系统名称'),
('system.version', '1.0.0', 'system', '系统版本'),
('system.description', '基于Spring Boot + Vue的现代化管理系统', 'system', '系统描述'),
('system.website', 'http://localhost:8080', 'system', '系统网址'),
('system.page_size', '10', 'system', '默认分页大小'),
('system.session_timeout', '30', 'system', '会话超时时间(分钟)'),
('system.maintenance_mode', 'false', 'system', '维护模式'),
('system.maintenance_message', '系统正在维护中，请稍后访问', 'system', '维护提示信息'),
('upload.max_size', '10', 'upload', '文件上传最大大小(MB)'),
('upload.allowed_types', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', 'upload', '允许上传的文件类型'),
('upload.avatar_max_size', '2', 'upload', '头像上传最大大小(MB)'),
('mail.smtp_host', 'smtp.qq.com', 'mail', 'SMTP服务器'),
('mail.smtp_port', '587', 'mail', 'SMTP端口'),
('mail.username', '', 'mail', '邮箱用户名'),
('mail.password', '', 'mail', '邮箱密码'),
('mail.from_name', 'Cursor Demo 系统', 'mail', '发件人名称')
ON DUPLICATE KEY UPDATE
setting_value = VALUES(setting_value),
setting_type = VALUES(setting_type),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP;

-- 创建文章分类表
CREATE TABLE IF NOT EXISTS `categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
  `slug` varchar(100) NOT NULL COMMENT '分类别名',
  `parent_id` int(11) DEFAULT 0 COMMENT '父分类ID',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `icon` varchar(50) DEFAULT NULL COMMENT '分类图标',
  `color` varchar(20) DEFAULT NULL COMMENT '分类颜色',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `article_count` int(11) DEFAULT 0 COMMENT '文章数量',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`),
  UNIQUE KEY `uk_category_slug` (`slug`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 创建文章表
CREATE TABLE IF NOT EXISTS `articles` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(255) NOT NULL COMMENT '文章标题',
  `content` longtext COMMENT '文章内容',
  `summary` text COMMENT '文章摘要',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `author_id` int(11) NOT NULL COMMENT '作者ID',
  `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
  `category_id` int(11) DEFAULT NULL COMMENT '分类ID',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态：draft草稿，published已发布，archived已归档',
  `view_count` int(11) DEFAULT 0 COMMENT '阅读量',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `comment_count` int(11) DEFAULT 0 COMMENT '评论数',
  `is_top` tinyint(1) DEFAULT 0 COMMENT '是否置顶',
  `is_recommend` tinyint(1) DEFAULT 0 COMMENT '是否推荐',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_is_recommend` (`is_recommend`),
  FULLTEXT KEY `idx_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 插入默认分类数据
INSERT INTO `categories` (`name`, `description`, `slug`, `parent_id`, `sort_order`, `icon`, `color`, `is_enabled`) VALUES
('技术分享', '技术相关的文章分类', 'tech', 0, 1, 'el-icon-cpu', '#409EFF', 1),
('生活随笔', '生活感悟和随笔', 'life', 0, 2, 'el-icon-coffee-cup', '#67C23A', 1),
('学习笔记', '学习过程中的笔记和总结', 'study', 0, 3, 'el-icon-notebook-2', '#E6A23C', 1),
('项目实战', '实际项目开发经验分享', 'project', 0, 4, 'el-icon-suitcase', '#F56C6C', 1),
('工具推荐', '好用工具和软件推荐', 'tools', 0, 5, 'el-icon-tools', '#909399', 1)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP;

-- 插入示例文章数据
INSERT INTO `articles` (`title`, `content`, `summary`, `author_id`, `author_name`, `category_id`, `category_name`, `tags`, `status`, `view_count`, `like_count`, `is_top`, `is_recommend`, `publish_time`) VALUES
('Vue 3 + Element Plus 后台管理系统开发指南', 
'本文详细介绍如何使用Vue 3和Element Plus构建现代化的后台管理系统。包括项目初始化、路由配置、组件开发、状态管理等方面的内容。通过本文，您可以快速掌握Vue 3的开发技巧，并构建出功能完善的管理系统。', 
'使用Vue 3和Element Plus构建现代化后台管理系统的完整指南。', 
1, 'admin', 1, '技术分享', 'Vue,Element Plus,后台管理,前端开发', 'published', 1250, 85, 1, 1, NOW()),

('Spring Boot 微服务架构实践', 
'本文分享Spring Boot微服务架构的实践经验，包括服务拆分、服务注册与发现、配置管理、负载均衡、熔断降级等核心技术。结合实际项目案例，深入讲解微服务开发的最佳实践。', 
'Spring Boot微服务架构实践经验分享，涵盖核心技术和最佳实践。', 
1, 'admin', 4, '项目实战', 'Spring Boot,微服务,架构,后端开发', 'published', 980, 67, 0, 1, NOW()),

('MySQL 性能优化最佳实践', 
'数据库性能优化是后端开发中的重要环节。本文总结了MySQL性能优化的各种方法，包括索引优化、查询优化、配置优化、硬件优化等方面。通过实际案例展示优化效果。', 
'MySQL数据库性能优化的完整指南和最佳实践。', 
1, 'admin', 3, '学习笔记', 'MySQL,性能优化,数据库,索引', 'published', 756, 42, 0, 1, NOW()),

('2024年前端开发工具推荐', 
'本文推荐了2024年最受欢迎的前端开发工具，包括代码编辑器、调试工具、构建工具、UI组件库等。每个工具都有详细的介绍和使用建议，帮助开发者提高工作效率。', 
'2024年最受欢迎的前端开发工具推荐和使用指南。', 
1, 'admin', 5, '工具推荐', '前端工具,开发效率,VSCode,调试工具', 'published', 625, 38, 0, 0, NOW()),

('程序员的工作生活平衡之道', 
'作为程序员，如何在繁忙的工作中保持生活的平衡？本文分享一些个人经验和思考，包括时间管理、健康管理、学习规划等方面的建议。', 
'程序员如何在工作和生活之间找到平衡的个人感悟。', 
1, 'admin', 2, '生活随笔', '工作生活平衡,时间管理,程序员生活', 'published', 445, 29, 0, 0, NOW()),

('Docker 容器化部署实战教程', 
'本文介绍如何使用Docker进行应用容器化部署，包括Dockerfile编写、镜像构建、容器运行、数据持久化、网络配置等内容。适合初学者快速上手Docker技术。', 
'Docker容器化部署的实战教程，从入门到实践。', 
1, 'admin', 1, '技术分享', 'Docker,容器化,部署,DevOps', 'draft', 0, 0, 0, 0, NULL)
ON DUPLICATE KEY UPDATE
title = VALUES(title),
content = VALUES(content),
updated_at = CURRENT_TIMESTAMP; 