<template>
  <div class="home-bg">
    <el-container class="home-container">
      <!-- 顶部导航栏 -->
      <el-header class="top-header">
        <div class="breadcrumb">
          <div class="system-title">
            <i class="el-icon-s-grid system-icon"></i>
            <span class="system-name">管理系统</span>
            <el-divider direction="vertical"></el-divider>
            <span class="current-page">{{ $route.meta.title || '仪表盘' }}</span>
          </div>
        </div>
        <div class="header-right">
          <MessageCenter />
          <div class="user-menu">
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="getAvatarUrl(user.avatar)">
                  <img src="/picture/2.JPG" />
                </el-avatar>
                <span class="username">{{ user.username }}</span>
                <i class="el-icon-arrow-down"></i>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile" icon="el-icon-user">个人中心</el-dropdown-item>
                  <el-dropdown-item command="settings" icon="el-icon-setting">设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout" icon="el-icon-switch-button">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <!-- 左侧菜单 -->
        <el-aside class="main-aside">
          <el-menu
            class="main-menu"
            :default-active="$route.path"
            router
            unique-opened
            background-color="#ffffff"
            text-color="#333"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/dashboard">
              <i class="el-icon-data-analysis"></i>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/user-management">
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/role-management">
              <i class="el-icon-user-solid"></i>
              <span>角色管理</span>
            </el-menu-item>
            <el-submenu index="content">
              <template #title>
                <i class="el-icon-document"></i>
                <span>内容管理</span>
              </template>
              <el-menu-item index="/article-list">
                <i class="el-icon-document-copy"></i>
                <span>文章列表</span>
              </el-menu-item>
              <el-menu-item index="/category-management">
                <i class="el-icon-folder"></i>
                <span>分类管理</span>
              </el-menu-item>
              <el-menu-item index="/qa-management">
                <i class="el-icon-chat-dot-round"></i>
                <span>通义千问AI</span>
              </el-menu-item>
            </el-submenu>
            <el-submenu index="statistics">
              <template #title>
                <i class="el-icon-pie-chart"></i>
                <span>数据统计</span>
              </template>
              <el-menu-item index="/visit-statistics">
                <i class="el-icon-view"></i>
                <span>访问统计</span>
              </el-menu-item>
              <el-menu-item index="/user-analysis">
                <i class="el-icon-data-line"></i>
                <span>用户分析</span>
              </el-menu-item>
            </el-submenu>
            <el-submenu index="system">
              <template #title>
                <i class="el-icon-setting"></i>
                <span>系统设置</span>
              </template>
              <el-menu-item index="/permission-management">
                <i class="el-icon-key"></i>
                <span>权限设置</span>
              </el-menu-item>
              <el-menu-item index="/basic-settings">
                <i class="el-icon-tools"></i>
                <span>基础设置</span>
              </el-menu-item>
              <el-menu-item index="/security-config">
                <i class="el-icon-lock"></i>
                <span>安全配置</span>
              </el-menu-item>
            </el-submenu>
            <el-menu-item index="/personal-center">
              <i class="el-icon-user-solid"></i>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 主内容区域 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MessageCenter from '../components/MessageCenter.vue'
import axios from 'axios'

export default {
  name: 'MainLayout',
  components: {
    MessageCenter
  },
  setup() {
    const router = useRouter()
    
    const user = ref({
      username: localStorage.getItem('username') || sessionStorage.getItem('username') || 'Admin',
      avatar: localStorage.getItem('avatar') || sessionStorage.getItem('avatar') || ''
    })

    // 获取用户完整信息
    const fetchUserInfo = async () => {
      try {
        const uid = localStorage.getItem('uid') || sessionStorage.getItem('uid')
        const token = localStorage.getItem('token') || sessionStorage.getItem('token')
        
        if (!uid || !token) {
          console.log('未找到用户ID或token，跳过获取用户信息')
          return
        }

        const res = await axios.get('/user/profile', {
          headers: {
            'Authorization': `Bearer ${token}`
          },
          params: {
            uid: parseInt(uid)
          }
        })

        if (res.data.code === 200) {
          const userInfo = res.data.data
          user.value.username = userInfo.username || user.value.username
          user.value.avatar = userInfo.avatar || ''
          
          // 保存头像到本地存储
          if (userInfo.avatar) {
            localStorage.setItem('avatar', userInfo.avatar)
            sessionStorage.setItem('avatar', userInfo.avatar)
          }
          
          console.log('获取用户信息成功:', userInfo)
        } else {
          console.warn('获取用户信息失败:', res.data.msg)
        }
      } catch (error) {
        console.error('获取用户信息异常:', error)
      }
    }

    // 生成头像URL的辅助函数
    const getAvatarUrl = (avatar) => {
      if (!avatar) {
        return '/picture/2.JPG' // 默认头像
      }
      
      // 如果是OSS的完整URL，直接返回
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        return avatar
      }
      
      // 兼容旧的相对路径格式
      return avatar.startsWith('/') ? avatar : `/${avatar}`
    }

    const handleCommand = (command) => {
      switch (command) {
        case 'profile':
          router.push('/personal-center')
          break
        case 'settings':
          router.push('/user-settings')
          break
        case 'logout':
          handleLogout()
          break
      }
    }

    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token')
        sessionStorage.removeItem('token')
        localStorage.removeItem('username')
        sessionStorage.removeItem('username')
        localStorage.removeItem('uid')
        sessionStorage.removeItem('uid')
        localStorage.removeItem('avatar')
        sessionStorage.removeItem('avatar')
        router.push('/login')
        ElMessage.success('退出成功')
      }).catch(() => {
        // 取消退出
      })
    }

    onMounted(() => {
      // 初始化用户信息
      const storedUsername = localStorage.getItem('username') || sessionStorage.getItem('username')
      if (storedUsername) {
        user.value.username = storedUsername
      }
      fetchUserInfo() // 在组件挂载时获取用户完整信息
      
      // 监听头像更新事件
      const handleStorageChange = (e) => {
        if (e.key === 'avatar_updated') {
          console.log('检测到头像更新，重新获取用户信息')
          fetchUserInfo()
          // 清除事件标记
          localStorage.removeItem('avatar_updated')
        }
      }
      
      // 添加存储变化监听器
      window.addEventListener('storage', handleStorageChange)
      
      // 组件卸载时移除监听器
      onBeforeUnmount(() => {
        window.removeEventListener('storage', handleStorageChange)
      })
    })

    return {
      user,
      handleCommand,
      getAvatarUrl
    }
  }
}
</script>

<style scoped>
.home-bg {
  min-height: 100vh;
  background: #f5f7fa;
}

.home-container {
  min-height: 100vh;
}

.top-header {
  background: #ffffff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px !important;
  line-height: 60px;
}

.breadcrumb {
  flex: 1;
  background: transparent;
  padding: 0;
}

.system-title {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.system-icon {
  font-size: 20px;
  color: #409EFF;
  margin-right: 8px;
}

.system-name {
  font-weight: 600;
  color: #2c3e50;
  margin-right: 12px;
}

.current-page {
  color: #606266;
  font-weight: 400;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-menu {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-aside {
  width: 200px !important;
  background: #ffffff;
  border-right: 1px solid #e6e6e6;
}

.main-menu {
  border-right: none;
  padding-top: 10px;
}

.main-content {
  background: #ffffff;
  padding: 20px;
  overflow-x: auto;
}

.el-menu-item, .el-submenu__title {
  height: 45px;
  line-height: 45px;
}

.el-menu-item i, .el-submenu__title i {
  margin-right: 8px;
}

@media (max-width: 768px) {
  .main-aside {
    width: 60px !important;
  }
  
  .main-menu .el-menu-item span,
  .main-menu .el-submenu__title span {
    display: none;
  }
  
  .username {
    display: none;
  }
  
  .system-title {
    font-size: 14px;
  }
  
  .system-name {
    display: none;
  }
  
  .current-page {
    font-size: 14px;
  }
}
</style> 