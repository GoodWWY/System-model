# 🚀 全栈管理系统 - 通义千问AI

一个基于Spring Boot + Vue 3的现代化全栈管理系统，集成了阿里云通义千问AI智能问答功能。

## ✨ 核心功能

### 🔐 用户管理系统
- 用户注册/登录（JWT认证）
- 用户信息管理（头像上传、个人资料编辑）
- 用户状态管理（启用/禁用）
- 用户活动日志追踪

### 👥 权限管理
- 基于RBAC的角色权限控制
- 角色分配和权限管理
- 细粒度权限控制

### 📝 内容管理
- 文章发布和管理
- 分类管理
- 富文本编辑

### 🤖 AI智能问答
- 集成阿里云通义千问API
- 支持多种模型选择（Turbo/Plus/Max/Max长文本）
- 实时对话聊天界面
- 问答历史记录

### 📊 数据统计
- 用户活动分析
- 访问统计图表
- 实时数据展示

## 🏗️ 技术栈

### 后端技术
- **框架**: Spring Boot 3.5.3
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: Spring Security + JWT
- **ORM**: MyBatis
- **文件存储**: 阿里云OSS
- **邮件服务**: Spring Mail
- **AI集成**: 阿里云通义千问API

### 前端技术
- **框架**: Vue 3 + Composition API
- **UI库**: Element Plus
- **图表**: ECharts
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **构建工具**: Vue CLI

## 🚀 快速开始

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

```bash
# 1. 导入数据库
mysql -u root -p < database_init.sql
mysql -u root -p < qa_system_tables.sql
mysql -u root -p < add_user_status_column.sql

# 2. 配置application.yml
# 修改数据库连接、Redis配置、阿里云OSS配置、通义千问API密钥等

# 3. 启动后端服务
cd back
mvn spring-boot:run
```

### 前端启动

```bash
# 1. 安装依赖
cd front
npm install

# 2. 启动开发服务器
npm run serve
```

### 访问应用
- 前端地址: http://localhost:8080
- 后端地址: http://localhost:8081

## 📋 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/management_system
    username: your_username
    password: your_password
```

### 通义千问AI配置
```yaml
qwen:
  api-key: your_qwen_api_key
  api-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
```

### 阿里云OSS配置
```yaml
aliyun:
  oss:
    endpoint: your_endpoint
    access-key-id: your_access_key
    access-key-secret: your_secret
    bucket-name: your_bucket
```

## 🎯 主要特性

### 🔒 安全特性
- JWT无状态认证
- 密码加密存储（BCrypt）
- CORS跨域配置
- SQL注入防护

### 🎨 用户体验
- 响应式设计，支持多端访问
- 实时数据更新
- 友好的错误提示
- 流畅的交互动画

### 🚀 性能优化
- Redis缓存机制
- 数据库连接池
- 前端路由懒加载
- 图片压缩和CDN加速

### 🤖 AI功能
- 多模型选择
- 流式对话体验
- 智能问答记录
- 错误重试机制

## 📁 项目结构

```
├── back/                 # 后端Spring Boot项目
│   ├── src/main/java/
│   │   └── com/wwy/
│   │       ├── Controller/    # 控制器层
│   │       ├── Service/       # 服务层  
│   │       ├── Entity/        # 实体类
│   │       ├── Mapper/        # 数据访问层
│   │       └── Common/        # 公共组件
│   └── src/main/resources/
│       ├── mapper/            # MyBatis映射文件
│       └── application.yml    # 配置文件
├── front/                # 前端Vue 3项目
│   ├── src/
│   │   ├── components/        # 通用组件
│   │   ├── views/             # 页面组件
│   │   ├── router/            # 路由配置
│   │   └── assets/            # 静态资源
│   └── public/                # 公共文件
└── *.sql                     # 数据库初始化脚本
```

## 🛠️ API接口

### 用户认证
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `POST /auth/logout` - 用户登出

### AI问答
- `POST /qa/ask` - 发送问题给AI
- `GET /qa/history` - 获取问答历史
- `GET /test/qwen` - 测试AI连接

### 用户管理
- `GET /users/list` - 获取用户列表
- `PUT /users/profile` - 更新用户资料
- `POST /users/avatar` - 上传头像

## 🔧 开发指南

### 添加新功能
1. 后端：创建Entity → Mapper → Service → Controller
2. 前端：创建Vue组件 → 配置路由 → 集成API

### 数据库迁移
```sql
-- 添加新表
CREATE TABLE new_table (...);

-- 更新现有表
ALTER TABLE existing_table ADD COLUMN new_column VARCHAR(255);
```

## 🐛 常见问题

### 1. AI问答不工作
- 检查通义千问API密钥是否正确
- 确认网络连接正常
- 查看后端日志错误信息

### 2. 图片上传失败
- 检查阿里云OSS配置
- 确认Bucket权限设置
- 验证文件大小限制

### 3. 跨域问题
- 检查WebConfig中的CORS配置
- 确认前端代理设置正确

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📞 联系方式

如有问题，请通过以下方式联系：
- 项目地址：https://github.com/GoodWWY/System-model
- Issues：https://github.com/GoodWWY/System-model/issues

---

⭐ 如果这个项目对您有帮助，请给个Star支持一下！ 