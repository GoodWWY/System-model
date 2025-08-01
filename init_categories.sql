USE demo;

-- 清空已有的分类数据
DELETE FROM categories;

-- 重置自增ID
ALTER TABLE categories AUTO_INCREMENT = 1;

-- 插入初始分类数据
INSERT INTO categories (name, description, slug, parent_id, sort_order, icon, color, is_enabled, article_count) VALUES 
('技术文章', '技术相关的文章分类', 'tech', 0, 1, 'Tools', '#409EFF', 1, 5),
('生活随笔', '记录生活点滴的文章', 'life', 0, 2, 'Document', '#67C23A', 1, 3),
('学习笔记', '学习过程中的记录和总结', 'study-notes', 0, 3, 'Edit', '#E6A23C', 1, 8),
('项目经验', '项目开发中的经验分享', 'project-exp', 0, 4, 'Setting', '#F56C6C', 1, 2),
('工具推荐', '好用工具的推荐和使用心得', 'tool-recommend', 0, 5, 'Star', '#909399', 1, 1),
('读书笔记', '读书心得和笔记整理', 'reading', 0, 6, 'Reading', '#8E44AD', 1, 4),
('旅行游记', '旅行中的见闻和感受', 'travel', 0, 7, 'LocationInformation', '#27AE60', 1, 0); 