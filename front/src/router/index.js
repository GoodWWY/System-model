import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '../components/Login.vue'
import RegisterPage from '../components/Register.vue'
import Layout from '../layout/Layout.vue'
import Dashboard from '../views/DashboardSimple.vue'
import UserManagement from '../views/UserManagement.vue'
import RoleManagement from '../views/RoleManagement.vue'
import PersonalCenter from '../views/PersonalCenter.vue'

const routes = [
  { 
    path: '/', 
    redirect: '/dashboard' 
  },
  { 
    path: '/login', 
    component: LoginPage 
  },
  { 
    path: '/register', 
    component: RegisterPage 
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { requiresAuth: true, title: '仪表盘' }
      },
      {
        path: 'user-management',
        name: 'UserManagement',
        component: UserManagement,
        meta: { requiresAuth: true, title: '用户管理' }
      },
      {
        path: 'role-management',
        name: 'RoleManagement', 
        component: RoleManagement,
        meta: { requiresAuth: true, title: '角色管理' }
      },
      {
        path: 'personal-center',
        name: 'PersonalCenter',
        component: PersonalCenter,
        meta: { requiresAuth: true, title: '个人中心' }
      },
      {
        path: 'user-settings',
        name: 'UserSettings',
        component: () => import('../views/UserSettings.vue'),
        meta: { requiresAuth: true, title: '用户设置' }
      },
      {
        path: 'permission-management',
        name: 'PermissionManagement',
        component: () => import('../views/PermissionManagement.vue'),
        meta: { requiresAuth: true, title: '权限设置' }
      },
      {
        path: 'article-list',
        name: 'ArticleList',
        component: () => import('../views/ArticleList.vue'),
        meta: { requiresAuth: true, title: '文章列表' }
      },
      {
        path: 'category-management',
        name: 'CategoryManagement',
        component: () => import('../views/CategoryManagement.vue'),
        meta: { requiresAuth: true, title: '分类管理' }
      },
      {
        path: 'qa-management',
        name: 'QaManagement',
        component: () => import('../views/QaManagement.vue'),
        meta: { requiresAuth: true, title: '通义千问AI' }
      },
      {
        path: 'visit-statistics',
        name: 'VisitStatistics',
        component: () => import('../views/VisitStatistics.vue'),
        meta: { requiresAuth: true, title: '访问统计' }
      },
      {
        path: 'user-analysis',
        name: 'UserAnalysis',
        component: () => import('../views/UserAnalysis.vue'),
        meta: { requiresAuth: true, title: '用户分析' }
      },
      {
        path: 'basic-settings',
        name: 'BasicSettings',
        component: () => import('../views/BasicSettings.vue'),
        meta: { requiresAuth: true, title: '基础设置' }
      },
      {
        path: 'security-config',
        name: 'SecurityConfig',
        component: () => import('../views/SecurityConfig.vue'),
        meta: { requiresAuth: true, title: '安全配置' }
      }
    ]
  },
  // 保留旧的/home路由以确保向后兼容
  { 
    path: '/home', 
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 获取token
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  const username = localStorage.getItem('username') || sessionStorage.getItem('username')
  
  // 需要验证的页面
  if (to.meta.requiresAuth) {
    // 检查是否有token和用户名
    if (!token || !username) {
      // 没有token或用户名，重定向到登录页
      next({ 
        path: '/login',
        query: { redirect: to.fullPath } // 保存原来想要访问的路径
      })
    } else {
      // 有token，验证token有效性（这里可以添加token验证逻辑）
      try {
        // 这里可以添加对token的验证，例如解析JWT或发送请求到后端验证
        // 如果token无效，应该清除token并重定向到登录页
        // 简单实现：检查token是否为空字符串
        if (token.trim() === '') {
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('username')
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          next('/login')
        } else {
          next() // token有效，允许访问
        }
      } catch (e) {
        // token解析失败，可能是无效的token
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('username')
        localStorage.removeItem('token')
        localStorage.removeItem('username')
    next('/login')
      }
    }
  } else {
    // 如果是访问登录页但已经有token，直接跳转到首页
    if (to.path === '/login' && token) {
      next('/dashboard')
    } else {
      next() // 不需要验证的页面，直接放行
    }
  }
})

export default router 