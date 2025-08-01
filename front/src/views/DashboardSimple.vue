<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>仪表板</h1>
      <p>欢迎回来！{{ username }}</p>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <h3>总用户数</h3>
          <p class="stat-number">{{ stats.totalUsers }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🆕</div>
        <div class="stat-info">
          <h3>今日新增</h3>
          <p class="stat-number">{{ stats.todayUsers }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">💚</div>
        <div class="stat-info">
          <h3>活跃用户</h3>
          <p class="stat-number">{{ stats.activeUsers }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-info">
          <h3>增长率</h3>
          <p class="stat-number">{{ stats.growthRate }}%</p>
        </div>
      </div>
    </div>

    <!-- 快捷操作区域 -->
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="goToUserManagement">
          <div class="action-icon">👤</div>
          <h3>用户管理</h3>
          <p>管理系统用户</p>
        </div>
        
        <div class="action-card" @click="goToRoleManagement">
          <div class="action-icon">🔑</div>
          <h3>角色管理</h3>
          <p>配置用户角色</p>
        </div>
        
        <div class="action-card" @click="goToPersonalCenter">
          <div class="action-icon">⚙️</div>
          <h3>个人中心</h3>
          <p>查看个人信息</p>
        </div>
        
        <div class="action-card" @click="goToSettings">
          <div class="action-icon">🛠️</div>
          <h3>系统设置</h3>
          <p>系统配置管理</p>
        </div>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities">
      <h2>最近活动</h2>
      <div class="activity-list">
        <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
          <div class="activity-time">{{ formatTime(activity.time) }}</div>
          <div class="activity-content">{{ activity.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

// 响应式数据
const username = ref(localStorage.getItem('username') || '用户')
const stats = ref({
  totalUsers: 0,
  todayUsers: 0,
  activeUsers: 0,
  growthRate: 0
})

const recentActivities = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    stats.value = {
      totalUsers: 1250,
      todayUsers: 45,
      activeUsers: 892,
      growthRate: 12.5
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取最近活动数据
const fetchRecentActivities = async () => {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    const response = await axios.get('/activity/recent?limit=10', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data && response.data.code === 200) {
      const activities = response.data.data || []
      recentActivities.value = activities.map(activity => ({
        id: activity.id,
        time: new Date(activity.createdAt),
        content: activity.actionDescription
      }))
    } else {
      console.warn('获取活动数据失败:', response.data?.message)
      // 如果API调用失败，使用默认数据
      recentActivities.value = [
        { id: 1, time: new Date(Date.now() - 1000 * 60 * 5), content: '暂无最近活动记录' }
      ]
    }
  } catch (error) {
    console.error('获取最近活动失败:', error)
    // 失败时显示默认消息
    recentActivities.value = [
      { id: 1, time: new Date(Date.now() - 1000 * 60 * 5), content: '暂无最近活动记录' }
    ]
  }
}

// 格式化时间
const formatTime = (time) => {
  const now = new Date()
  const diff = Math.floor((now - time) / 1000 / 60) // 分钟差
  
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return time.toLocaleDateString()
}

// 导航方法
const goToUserManagement = () => {
  router.push('/user-management')
}

const goToRoleManagement = () => {
  router.push('/role-management')
}

const goToPersonalCenter = () => {
  router.push('/personal-center')
}

const goToSettings = () => {
  router.push('/basic-settings')
}

// 生命周期
onMounted(() => {
  fetchStats()
  fetchRecentActivities()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 28px;
}

.page-header p {
  color: #666;
  margin: 0;
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f8ff;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 14px;
  font-weight: normal;
}

.stat-number {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.quick-actions, .recent-activities {
  margin-bottom: 30px;
}

.quick-actions h2, .recent-activities h2 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 20px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.action-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.action-card h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.action-card p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.activity-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  color: #999;
  font-size: 12px;
  min-width: 80px;
  margin-right: 20px;
}

.activity-content {
  color: #333;
  font-size: 14px;
}
</style> 