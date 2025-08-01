<template>
  <div class="dashboard-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>欢迎回来，{{ username }}</h1>
        <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
        <el-button type="primary" class="welcome-btn" @click="onUserListClick">查看用户列表</el-button>
      </div>
      <div class="welcome-decoration">
        <el-avatar 
          :size="150" 
          :src="getAvatarUrl(currentUserAvatar)" 
          @error="() => true"
          class="decoration-image"
        >
          <img src="/picture/2.JPG" />
        </el-avatar>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-card-value">1,286</div>
          <div class="stat-card-title">总用户数</div>
          <div class="stat-card-trend">
            <span class="trend-up">
              <el-icon><TopRight /></el-icon> 12%
            </span>
            较上周
          </div>
        </div>
        <div class="stat-card-icon user-icon">
          <el-icon><User /></el-icon>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-card-value">256</div>
          <div class="stat-card-title">活跃用户</div>
          <div class="stat-card-trend">
            <span class="trend-down">
              <el-icon><BottomRight /></el-icon> 5%
            </span>
            较上周
          </div>
        </div>
        <div class="stat-card-icon visit-icon">
          <el-icon><View /></el-icon>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-card-value">86</div>
          <div class="stat-card-title">新增用户</div>
          <div class="stat-card-trend">
            <span class="trend-up">
              <el-icon><TopRight /></el-icon> 15%
            </span>
            较上周
          </div>
        </div>
        <div class="stat-card-icon new-user-icon">
          <el-icon><Plus /></el-icon>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-card-value">98%</div>
          <div class="stat-card-title">系统运行</div>
          <div class="stat-card-trend">
            <span class="trend-stable">
              <el-icon><Right /></el-icon> 稳定
            </span>
          </div>
        </div>
        <div class="stat-card-icon system-icon">
          <el-icon><Monitor /></el-icon>
        </div>
      </el-card>
    </div>
    
    <!-- 图表和活动区域 -->
    <div class="dashboard-main">
      <!-- 图表区域 -->
      <div class="chart-section">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>用户增长趋势</span>
              <el-dropdown size="small" @command="handlePeriodChange">
                <span class="el-dropdown-link">
                  {{ currentPeriod }} <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="今日">今日</el-dropdown-item>
                    <el-dropdown-item command="本周">本周</el-dropdown-item>
                    <el-dropdown-item command="本月">本月</el-dropdown-item>
                    <el-dropdown-item command="今年">今年</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          <div ref="userGrowthChart" class="chart-container"></div>
        </el-card>
      </div>
      
      <!-- 右侧区域：活动和快捷操作 -->
      <div class="side-section">
        <!-- 最近活动 -->
        <el-card shadow="hover" class="recent-activity">
          <template #header>
            <div class="activity-header">
              <span>最近活动</span>
              <el-button type="text" @click="viewAllActivities">查看全部</el-button>
            </div>
          </template>
          <div class="activity-list">
            <el-empty v-if="recentActivities.length === 0" description="暂无活动记录" :image-size="60"></el-empty>
            <div v-else v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
              <div class="activity-content">
                <div class="activity-title">{{ activity.username }} {{ activity.action }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 快捷操作 -->
        <el-card shadow="hover" class="quick-actions">
          <template #header>
            <div class="action-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="action-grid">
            <div class="action-item" @click="onUserListClick">
              <el-icon class="action-icon"><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="action-item" @click="openSystemSettings">
              <el-icon class="action-icon"><Setting /></el-icon>
              <span>系统设置</span>
            </div>
            <div class="action-item" @click="openContentManagement">
              <el-icon class="action-icon"><Document /></el-icon>
              <span>内容管理</span>
            </div>
            <div class="action-item" @click="openDataStatistics">
              <el-icon class="action-icon"><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, getCurrentInstance, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'DashboardView',
  setup() {
    const router = useRouter()
    const username = ref(localStorage.getItem('username') || sessionStorage.getItem('username') || '')
    const currentUserAvatar = ref('')
    const currentDate = computed(() => {
      const date = new Date()
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
    })

    // 图表相关
    const userGrowthChart = ref(null)
    const currentPeriod = ref('本月')
    
    // 初始化所有图表
    const initCharts = () => {
      // 增加延迟初始化，确保DOM已经完全渲染
      setTimeout(() => {
        nextTick(() => {
          try {
            if (userGrowthChart.value) {
              initUserGrowthChart()
              console.log('初始化用户增长趋势图表成功')
            } else {
              console.error('userGrowthChart元素不存在')
            }
          } catch (error) {
            console.error('初始化图表失败:', error)
          }
        })
      }, 300)
    }
    
    // 初始化用户增长趋势图
    const initUserGrowthChart = () => {
      console.log('开始初始化用户增长趋势图')
      if (!userGrowthChart.value) {
        console.error('用户增长趋势图DOM元素不存在')
        return
      }
      
      // 使用getCurrentInstance获取全局挂载的echarts
      const instance = getCurrentInstance()
      if (!instance) {
        console.error('getCurrentInstance() 返回 null，无法访问全局属性')
        return
      }
      const { proxy } = instance
      if (!proxy || !proxy.$echarts) {
        console.error('ECharts未在Vue实例上正确挂载')
        return
      }
      const echarts = proxy.$echarts
      console.log('ECharts实例已获取')
      
      const chart = echarts.init(userGrowthChart.value)
      
      // 根据当前选择的时间段获取不同数据
      const data = getUserGrowthData(currentPeriod.value)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.xAxis,
          axisLabel: {
            interval: data.interval || 'auto',
            rotate: data.rotate || 0
          }
        },
        yAxis: {
          type: 'value',
          name: '用户数',
          minInterval: 1
        },
        series: [
          {
            name: '新增用户',
            type: 'bar',
            data: data.newUsers,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#2378f7' },
                  { offset: 0.7, color: '#2378f7' },
                  { offset: 1, color: '#83bff6' }
                ])
              }
            }
          },
          {
            name: '活跃用户',
            type: 'line',
            smooth: true,
            data: data.activeUsers,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3,
              color: '#FF9800'
            },
            itemStyle: {
              color: '#FF9800'
            }
          }
        ],
        legend: {
          data: ['新增用户', '活跃用户'],
          right: '10%',
          top: '2%'
        }
      }
      
      chart.setOption(option)
      
      // 监听窗口变化，调整图表大小
      window.addEventListener('resize', () => {
        chart.resize()
      })
      
      // 存储图表实例，以便后续更新
      userGrowthChartInstance.value = chart
    }
    
    // 处理时间段变更
    const handlePeriodChange = (command) => {
      currentPeriod.value = command
      // 更新图表数据
      const data = getUserGrowthData(command)
      
      // 安全检查
      if (!userGrowthChartInstance.value) {
        console.warn('图表实例不存在，尝试重新初始化...')
        try {
          initUserGrowthChart()
          return
        } catch (error) {
          console.error('重新初始化图表失败:', error)
          return
        }
      }
      
      try {
        userGrowthChartInstance.value.setOption({
          xAxis: {
            data: data.xAxis,
            axisLabel: {
              interval: data.interval || 'auto',
              rotate: data.rotate || 0
            }
          },
          series: [
            {
              name: '新增用户',
              data: data.newUsers
            },
            {
              name: '活跃用户',
              data: data.activeUsers
            }
          ]
        })
      } catch (error) {
        console.error('更新增长趋势图表失败:', error)
      }
    }
    
    // 图表实例存储
    const userGrowthChartInstance = ref(null)
    
    // 获取用户增长趋势数据
    const getUserGrowthData = (period) => {
      // 根据选择的时间段返回不同的数据
      switch (period) {
        case '今日':
          return {
            xAxis: ['0时', '3时', '6时', '9时', '12时', '15时', '18时', '21时', '现在'],
            newUsers: [2, 1, 0, 8, 6, 9, 12, 5, 3],
            activeUsers: [15, 8, 5, 30, 35, 40, 50, 42, 28],
            interval: 0
          }
        case '本周':
          return {
            xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
            newUsers: [20, 18, 25, 22, 30, 15, 10],
            activeUsers: [120, 132, 101, 134, 190, 230, 210]
          }
        case '今年':
          return {
            xAxis: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            newUsers: [86, 75, 90, 110, 95, 120, 95, 85, 100, 110, 130, 120],
            activeUsers: [580, 600, 680, 750, 800, 850, 900, 920, 970, 1050, 1150, 1286],
            interval: 0
          }
        case '本月':
        default: {
          // 生成当月天数数据
          const days = Array.from({length: 30}, (_, i) => `${i+1}日`);
          return {
            xAxis: days,
            newUsers: [5, 7, 3, 8, 10, 6, 9, 11, 4, 6, 12, 8, 7, 9, 5, 3, 6, 8, 10, 7, 4, 6, 9, 12, 8, 5, 7, 10, 11, 8],
            activeUsers: [50, 60, 45, 70, 80, 75, 90, 100, 85, 95, 110, 100, 90, 95, 80, 70, 85, 95, 105, 90, 80, 85, 100, 120, 110, 90, 100, 110, 120, 115],
            interval: 2,
            rotate: 45
          };
        }
      }
    }
    
    // 最近活动数据
    const recentActivities = ref([
      {
        username: '张三',
        action: '登录了系统',
        time: formatTime(new Date(Date.now() - 2 * 60 * 1000)) // 2分钟前
      },
      {
        username: '李四',
        action: '更新了个人资料',
        time: formatTime(new Date(Date.now() - 60 * 60 * 1000)) // 1小时前
      },
      {
        username: '王五',
        action: '上传了新文件',
        time: formatTime(new Date(Date.now() - 3 * 60 * 60 * 1000)) // 3小时前
      },
      {
        username: '赵六',
        action: '评论了文章',
        time: '昨天'
      }
    ])
    
    // 获取最近活动
    const fetchRecentActivities = async () => {
      try {
        // 这里可以替换为实际的API调用
        // const response = await axios.get('/api/activities/recent')
        // recentActivities.value = response.data
        
        // 不再处理头像，因为已从界面中移除
        // 模拟获取真实数据
        console.log('已获取最近活动数据')
      } catch (error) {
        console.error('获取最近活动失败:', error)
      }
    }
    
    // 查看所有活动
    const viewAllActivities = () => {
      ElMessage.info('查看所有活动功能即将上线')
    }
    
    // 快捷操作功能
    const openSystemSettings = () => {
      ElMessage.info('系统设置功能即将上线')
    }
    
    const openContentManagement = () => {
      ElMessage.info('内容管理功能即将上线')
    }
    
    const openDataStatistics = () => {
      ElMessage.info('数据统计功能即将上线')
    }
    
    // 格式化时间
    function formatTime(date) {
      const now = new Date()
      const diff = now - date
      
      // 小于1分钟
      if (diff < 60 * 1000) {
        return '刚刚'
      }
      
      // 小于1小时
      if (diff < 60 * 60 * 1000) {
        return `${Math.floor(diff / (60 * 1000))}分钟前`
      }
      
      // 小于24小时
      if (diff < 24 * 60 * 60 * 1000) {
        return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
      }
      
      // 小于30天
      if (diff < 30 * 24 * 60 * 60 * 1000) {
        return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
      }
      
      // 大于30天，显示具体日期
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
    }
    
    const onUserListClick = () => {
      router.push('/user-list')
    }
    
    const getAvatarUrl = (url) => {
      if (!url) {
        return '/picture/2.JPG'
      }
      // 如果是OSS的完整URL，直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      // 兼容旧的相对路径格式
      return url.startsWith('/') ? url : `/${url}`
    }
    
    onMounted(() => {
      // 检查头像
      currentUserAvatar.value = localStorage.getItem('avatar') || sessionStorage.getItem('avatar') || '/picture/2.JPG'
      
      // 获取用户信息
      const token = localStorage.getItem('token') || sessionStorage.getItem('token')
      if (token) {
        axios.get('/user/profile', {
          headers: { Authorization: `Bearer ${token}` }
        }).then(res => {
          if (res.data.code === 200) {
            currentUserAvatar.value = res.data.data.avatar || '/picture/2.JPG'
          }
        }).catch(err => {
          console.error('获取用户信息失败:', err)
        })
      }
      
      // 初始化图表
      setTimeout(() => {
        try {
          const instance = getCurrentInstance()
          if (!instance || !instance.proxy || !instance.proxy.$echarts) {
            console.error('ECharts库未加载或全局实例未初始化')
          } else {
            console.log('ECharts库已成功加载')
            initCharts()
          }
        } catch (error) {
          console.error('初始化图表失败:', error)
        }
        
        // 获取最近活动数据
        fetchRecentActivities()
      }, 1000)
    })
    
    return {
      username,
      currentUserAvatar,
      currentDate,
      userGrowthChart,
      currentPeriod,
      handlePeriodChange,
      recentActivities,
      viewAllActivities,
      onUserListClick,
      openSystemSettings,
      openContentManagement,
      openDataStatistics,
      getAvatarUrl
    }
  }
}
</script>

<style scoped>
/* 仪表盘容器 */
.dashboard-container {
  padding: 16px;
}

/* 欢迎横幅 */
.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1976d2 0%, #64b5f6 100%);
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 24px;
  color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
}

.welcome-content {
  flex: 2;
  z-index: 1;
}

.welcome-content h1 {
  font-size: 28px;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.welcome-content p {
  font-size: 16px;
  margin-bottom: 20px;
  opacity: 0.9;
}

.welcome-btn {
  margin-top: 10px;
  padding: 12px 24px;
  font-weight: 500;
}

.welcome-decoration {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.decoration-image {
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
}

.welcome-decoration :deep(.el-avatar) {
  --el-avatar-bg-color: transparent;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  border-radius: 12px;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.stat-card-content {
  padding: 10px;
  z-index: 1;
}

.stat-card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 5px;
}

.stat-card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
}

.stat-card-trend {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

.trend-up {
  color: #67C23A;
  display: flex;
  align-items: center;
  gap: 2px;
}

.trend-down {
  color: #F56C6C;
  display: flex;
  align-items: center;
  gap: 2px;
}

.trend-stable {
  color: #409EFF;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 8px;
  color: white;
  margin: 10px;
  font-size: 24px;
}

.user-icon {
  background: linear-gradient(135deg, #4CAF50 0%, #8BC34A 100%);
}

.visit-icon {
  background: linear-gradient(135deg, #2196F3 0%, #03A9F4 100%);
}

.new-user-icon {
  background: linear-gradient(135deg, #FF9800 0%, #FFC107 100%);
}

.system-icon {
  background: linear-gradient(135deg, #E91E63 0%, #F44336 100%);
}

/* 图表区域美化 */
.dashboard-main {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.chart-container {
  height: 350px;
  width: 100%;
  border-radius: 8px;
}

/* 活动区域美化 */
.side-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-header, .action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 0 5px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 3px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 快捷操作区 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #f5f7fa;
}

.action-item:hover {
  background-color: #ecf5ff;
  transform: translateY(-3px);
}

.action-icon {
  font-size: 24px;
  margin-bottom: 10px;
  color: #409EFF;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .dashboard-main {
    grid-template-columns: 1fr;
  }
}
</style> 