USE demo;

-- 权限表
CREATE TABLE IF NOT EXISTS `permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `code` varchar(100) NOT NULL COMMENT '权限编码',
  `description` varchar(255) DEFAULT NULL COMMENT '权限描述',
  `resource` varchar(255) DEFAULT NULL COMMENT '资源路径',
  `action` varchar(50) DEFAULT NULL COMMENT '操作类型(CREATE,READ,UPDATE,DELETE,ALL)',
  `category` varchar(50) DEFAULT 'API' COMMENT '权限分类(MENU,BUTTON,API)',
  `parent_id` int(11) DEFAULT 0 COMMENT '父权限ID',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category` (`category`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  `permission_id` int(11) NOT NULL COMMENT '权限ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 插入基础权限数据
INSERT INTO `permissions` (`name`, `code`, `description`, `resource`, `action`, `category`, `parent_id`, `sort_order`) VALUES
-- 用户管理权限
('用户管理', 'user:manage', '用户管理模块权限', '/user-management', 'ALL', 'MENU', 0, 1),
('查看用户', 'user:view', '查看用户列表权限', '/user/search', 'READ', 'API', 1, 11),
('创建用户', 'user:create', '创建用户权限', '/user', 'CREATE', 'API', 1, 12),
('编辑用户', 'user:update', '编辑用户权限', '/user/*', 'UPDATE', 'API', 1, 13),
('删除用户', 'user:delete', '删除用户权限', '/user/*', 'DELETE', 'API', 1, 14),
('导出用户', 'user:export', '导出用户数据权限', '/user/export', 'READ', 'API', 1, 15),

-- 角色管理权限
('角色管理', 'role:manage', '角色管理模块权限', '/role-management', 'ALL', 'MENU', 0, 2),
('查看角色', 'role:view', '查看角色列表权限', '/role/list', 'READ', 'API', 7, 21),
('创建角色', 'role:create', '创建角色权限', '/role', 'CREATE', 'API', 7, 22),
('编辑角色', 'role:update', '编辑角色权限', '/role/*', 'UPDATE', 'API', 7, 23),
('删除角色', 'role:delete', '删除角色权限', '/role/*', 'DELETE', 'API', 7, 24),
('分配权限', 'role:assign', '为角色分配权限', '/role/assign', 'UPDATE', 'API', 7, 25),

-- 权限管理权限
('权限设置', 'permission:manage', '权限设置模块权限', '/permission-management', 'ALL', 'MENU', 0, 3),
('查看权限', 'permission:view', '查看权限列表权限', '/permission/list', 'READ', 'API', 13, 31),
('创建权限', 'permission:create', '创建权限权限', '/permission', 'CREATE', 'API', 13, 32),
('编辑权限', 'permission:update', '编辑权限权限', '/permission/*', 'UPDATE', 'API', 13, 33),
('删除权限', 'permission:delete', '删除权限权限', '/permission/*', 'DELETE', 'API', 13, 34),

-- 分类管理权限
('分类管理', 'category:manage', '分类管理模块权限', '/category-management', 'ALL', 'MENU', 0, 4),
('查看分类', 'category:view', '查看分类列表权限', '/category/search', 'READ', 'API', 18, 41),
('创建分类', 'category:create', '创建分类权限', '/category', 'CREATE', 'API', 18, 42),
('编辑分类', 'category:update', '编辑分类权限', '/category/*', 'UPDATE', 'API', 18, 43),
('删除分类', 'category:delete', '删除分类权限', '/category/*', 'DELETE', 'API', 18, 44),

-- 文章管理权限
('文章列表', 'article:manage', '文章列表模块权限', '/article-list', 'ALL', 'MENU', 0, 5),
('查看文章', 'article:view', '查看文章列表权限', '/article/search', 'READ', 'API', 23, 51),
('创建文章', 'article:create', '创建文章权限', '/article', 'CREATE', 'API', 23, 52),
('编辑文章', 'article:update', '编辑文章权限', '/article/*', 'UPDATE', 'API', 23, 53),
('删除文章', 'article:delete', '删除文章权限', '/article/*', 'DELETE', 'API', 23, 54),

-- 系统设置权限
('基础设置', 'system:manage', '基础设置模块权限', '/basic-settings', 'ALL', 'MENU', 0, 6),
('系统配置', 'system:config', '系统配置权限', '/system/*', 'ALL', 'API', 28, 61),
('缓存管理', 'cache:manage', '缓存管理权限', '/cache/*', 'ALL', 'API', 28, 62),

-- 个人中心权限
('个人中心', 'profile:manage', '个人中心权限', '/personal-center', 'ALL', 'MENU', 0, 7),
('修改密码', 'profile:password', '修改密码权限', '/user/change-password', 'UPDATE', 'API', 31, 71),
('更新资料', 'profile:update', '更新个人资料权限', '/user/profile', 'UPDATE', 'API', 31, 72);

-- 为默认角色分配权限（假设管理员角色ID为1）
INSERT INTO `role_permissions` (`role_id`, `permission_id`) 
SELECT 1, id FROM `permissions` WHERE id <= 32;

-- 为普通用户角色分配基础权限（假设用户角色ID为2）
INSERT INTO `role_permissions` (`role_id`, `permission_id`) 
SELECT 2, id FROM `permissions` WHERE code IN ('user:view', 'role:view', 'category:view', 'article:view', 'profile:manage', 'profile:password', 'profile:update'); 