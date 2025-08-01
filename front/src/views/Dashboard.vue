<template>
  <div class="dashboard-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>欢迎回来，{{ username }}</h1>
        <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
        <el-button type="primary" class="welcome-btn" @click="goToUserList">查看用户列表</el-button>
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
          <div class="stat-card-title">今日访问</div>
          <div class="stat-card-trend">
            <span class="trend-up">
              <el-icon><TopRight /></el-icon> 8%
            </span>
            较昨日
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
            <span class="trend-down">
              <el-icon><BottomRight /></el-icon> 3%
            </span>
            较昨日
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
          <div class="chart-wrapper">
            <div ref="userGrowthChart" class="chart-container"></div>
            <div v-if="!chartInstance || isChartInitializing" class="chart-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <p>图表加载中...</p>
            </div>
          </div>
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
            <div class="action-item" @click="goToUserList">
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
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'

export default {
  name: 'DashboardView',
  setup() {
    const router = useRouter()
    
    const username = ref(localStorage.getItem('username') || sessionStorage.getItem('username') || '')
    const currentUserAvatar = ref('')
    const userGrowthChart = ref(null)
    const currentPeriod = ref('本月')
    const recentActivities = ref([])
    const chartInstance = ref(null)
    const isChartInitializing = ref(false)
    let resizeHandler = null
    
    // 获取当前日期
    const today = new Date()
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
    const currentDate = today.toLocaleDateString('zh-CN', options)
    
    // 获取头像URL
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
    
    // 跳转到用户列表
    const goToUserList = () => {
      router.push('/user-management')
    }
    
    // 生成图表数据
    const generateChartData = (period) => {
      let data = []
      let categories = []
      
      switch (period) {
        case '今日':
          // 生成24小时数据
          for (let i = 0; i < 24; i++) {
            categories.push(`${i.toString().padStart(2, '0')}:00`)
            data.push(Math.floor(Math.random() * 30) + 10)
          }
          break
        case '本周': {
          // 生成7天数据
          const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
          weekDays.forEach(day => {
            categories.push(day)
            data.push(Math.floor(Math.random() * 100) + 50)
          })
          break
        }
        case '本月':
          // 生成30天数据
          for (let i = 1; i <= 30; i++) {
            categories.push(`${i}日`)
            data.push(Math.floor(Math.random() * 80) + 20)
          }
          break
        case '今年': {
          // 生成12个月数据
          const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
          months.forEach(month => {
            categories.push(month)
            data.push(Math.floor(Math.random() * 500) + 200)
          })
          break
        }
      }
      
      return { data, categories }
    }

    // 处理时间段变化
    const handlePeriodChange = (period) => {
      currentPeriod.value = period
      updateChart(period)
      ElMessage.success(`已切换到${period}视图`)
    }
    
    // 查看全部活动
    const viewAllActivities = () => {
      ElMessage.info('查看全部活动功能开发中')
    }
    
    // 快捷操作
    const openSystemSettings = () => {
      router.push('/basic-settings')
    }
    
    const openContentManagement = () => {
      ElMessage.info('内容管理功能开发中')
    }
    
    const openDataStatistics = () => {
      ElMessage.info('数据统计功能开发中')
    }
    
    // 获取用户头像
    const fetchUserAvatar = async () => {
      const uid = localStorage.getItem('uid') || sessionStorage.getItem('uid')
      if (!uid) {
        currentUserAvatar.value = '/picture/2.JPG'
        return
      }
      
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const response = await axios.get(`/user/profile?uid=${uid}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        })
        
        if (response.data && response.data.code === 200 && response.data.data) {
          const avatar = response.data.data.avatar
          currentUserAvatar.value = avatar || '/picture/2.JPG'
        } else {
          currentUserAvatar.value = '/picture/2.JPG'
        }
      } catch (error) {
        console.error('获取用户头像出错:', error)
        currentUserAvatar.value = '/picture/2.JPG'
      }
    }
    
    // 获取用户增长数据 (可选择从后端API获取真实数据)
    const fetchUserGrowthData = async (period) => {
      try {
        // 这里可以调用后端API获取真实的用户增长数据
        // const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        // const response = await axios.get(`/statistics/user-growth?period=${period}`, {
        //   headers: { Authorization: `Bearer ${token}` }
        // })
        // return response.data.data
        
        // 暂时使用模拟数据
        return generateChartData(period)
      } catch (error) {
        console.error('获取用户增长数据失败:', error)
        // 出错时返回默认数据
        return generateChartData(period)
      }
    }
    
    // 获取最近活动
    const fetchRecentActivities = async () => {
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token')
        const response = await axios.get('/activity/recent?limit=6', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        if (response.data && response.data.code === 200) {
          const activities = response.data.data || []
          recentActivities.value = activities.map(activity => {
            // 计算时间差
            const activityTime = new Date(activity.createdAt)
            const now = new Date()
            const diff = Math.floor((now - activityTime) / 1000 / 60) // 分钟差
            
            let timeText = ''
            if (diff < 1) timeText = '刚刚'
            else if (diff < 60) timeText = `${diff}分钟前`
            else if (diff < 1440) timeText = `${Math.floor(diff / 60)}小时前`
            else timeText = activityTime.toLocaleDateString()
            
            return {
              username: activity.username,
              action: activity.actionDescription,
              time: timeText
            }
          })
        } else {
          console.warn('获取活动数据失败:', response.data?.message)
          // 如果API调用失败，显示默认消息
          recentActivities.value = [
            {
              username: '系统',
              action: '暂无最近活动记录',
              time: '刚刚'
            }
          ]
        }
      } catch (error) {
        console.error('获取最近活动失败:', error)
        // 失败时显示默认消息
        recentActivities.value = [
          {
            username: '系统',
            action: '暂无最近活动记录',
            time: '刚刚'
          }
        ]
      }
    }
    
    // 初始化图表
    const initCharts = () => {
      // 防止重复初始化
      if (isChartInitializing.value || chartInstance.value) {
        return
      }
      
      // 更严格的DOM元素检查
      if (!userGrowthChart.value) {
        console.warn('图表DOM元素未找到，延迟重试')
        setTimeout(() => {
          initCharts()
        }, 100)
        return
      }
      
      // 检查元素是否已经挂载到DOM
      if (!userGrowthChart.value.parentNode) {
        console.warn('图表DOM元素未完全挂载，延迟重试')
        setTimeout(() => {
          initCharts()
        }, 100)
        return
      }
      
      isChartInitializing.value = true
      
      try {
        // 如果已经存在实例，先销毁
        if (chartInstance.value) {
          chartInstance.value.dispose()
          chartInstance.value = null
        }
        
        // 初始化ECharts实例
        chartInstance.value = echarts.init(userGrowthChart.value)
        
        // 更新图表
        updateChart(currentPeriod.value)
        
        // 监听窗口大小变化
        resizeHandler = () => {
          if (chartInstance.value) {
            chartInstance.value.resize()
          }
        }
        window.addEventListener('resize', resizeHandler)
        
        console.log('ECharts图表初始化成功')
      } catch (error) {
        console.error('图表初始化失败:', error)
        ElMessage.error('图表初始化失败，请刷新页面重试')
        chartInstance.value = null
      } finally {
        isChartInitializing.value = false
      }
    }
    
    // 更新图表
    const updateChart = async (period) => {
      if (!chartInstance.value) return
      
      try {
        const { data, categories } = await fetchUserGrowthData(period)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          borderColor: '#e4e7ed',
          borderWidth: 1,
          textStyle: {
            color: '#606266'
          },
          formatter: function(params) {
            const point = params[0]
            return `${point.name}<br/>${point.seriesName}: ${point.value}`
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
          boundaryGap: false,
          data: categories,
          axisLabel: {
            color: '#909399',
            fontSize: 12
          },
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          splitLine: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#909399',
            fontSize: 12
          },
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          splitLine: {
            lineStyle: {
              color: '#f5f7fa',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '新增用户',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              color: '#409eff',
              width: 3
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                  { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
                ]
              }
            },
            itemStyle: {
              color: '#409eff',
              borderColor: '#ffffff',
              borderWidth: 2
            },
            data: data
          }
        ]
      }
      
        chartInstance.value.setOption(option, true)
      } catch (error) {
        console.error('更新图表失败:', error)
        ElMessage.error('图表更新失败，请稍后重试')
      }
    }
    
    onMounted(() => {
      fetchUserAvatar()
      fetchRecentActivities()
      
      // 确保DOM完全渲染后再初始化图表
      nextTick(() => {
        // 使用更长的延迟确保DOM完全准备好
        setTimeout(() => {
          if (userGrowthChart.value) {
            initCharts()
          } else {
            console.warn('图表容器元素未找到，跳过图表初始化')
          }
        }, 800)
      })
    })
    
    onUnmounted(() => {
      // 销毁图表实例
      if (chartInstance.value) {
        chartInstance.value.dispose()
        chartInstance.value = null
      }
      
      // 移除窗口大小变化监听器
      if (resizeHandler) {
        window.removeEventListener('resize', resizeHandler)
        resizeHandler = null
      }
    })
    
    return {
      username,
      currentUserAvatar,
      userGrowthChart,
      currentPeriod,
      recentActivities,
      currentDate,
      chartInstance,
      isChartInitializing,
      getAvatarUrl,
      goToUserList,
      handlePeriodChange,
      viewAllActivities,
      openSystemSettings,
      openContentManagement,
      openDataStatistics,
      generateChartData,
      updateChart,
      fetchUserGrowthData
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 100%;
  overflow-x: hidden;
  min-height: calc(100vh - 120px);
}

.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.welcome-content h1 {
  font-size: 24px;
  margin: 0 0 8px 0;
  font-weight: 600;
  color: #303133;
}

.welcome-content p {
  font-size: 14px;
  margin: 0 0 16px 0;
  color: #606266;
}

.welcome-btn {
  background: #409eff;
  border: 1px solid #409eff;
  color: white;
  font-size: 14px;
  padding: 8px 16px;
}

.welcome-btn:hover {
  background: #66b1ff;
  border: 1px solid #66b1ff;
}

.decoration-image {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card-content {
  padding: 20px;
  position: relative;
  z-index: 2;
}

.stat-card-value {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8px;
}

.stat-card-title {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 12px;
}

.stat-card-trend {
  font-size: 12px;
  color: #95a5a6;
}

.trend-up {
  color: #27ae60;
}

.trend-down {
  color: #e74c3c;
}

.trend-stable {
  color: #3498db;
}

.stat-card-icon {
  position: absolute;
  right: 20px;
  top: 20px;
  font-size: 40px;
  opacity: 0.1;
}

.user-icon {
  color: #3498db;
}

.visit-icon {
  color: #27ae60;
}

.new-user-icon {
  color: #f39c12;
}

.system-icon {
  color: #9b59b6;
}

.dashboard-main {
  display: grid;
  grid-template-columns: 1.8fr 1fr;
  gap: 16px;
  max-width: 100%;
}

.chart-section {
  display: flex;
  flex-direction: column;
}

.chart-card {
  height: 400px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header span {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.el-dropdown-link:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.chart-wrapper {
  height: 320px;
  width: 100%;
  position: relative;
}

.chart-container {
  height: 100%;
  width: 100%;
  background: #ffffff;
  border-radius: 4px;
}

.chart-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #909399;
}

.chart-loading .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.chart-loading p {
  margin: 0;
  font-size: 14px;
}

.side-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recent-activity {
  height: 240px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-list {
  max-height: 180px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.quick-actions {
  height: 160px;
}

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
  padding: 16px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  min-height: 60px;
}

.action-item:hover {
  background-color: #f8f9fa;
  border-color: #409eff;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 22px;
  color: #409eff;
  margin-bottom: 6px;
}

.action-item span {
  font-size: 12px;
  color: #666;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }
  
  .dashboard-main {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding: 20px;
  }
  
  .decoration-image {
    width: 100px !important;
    height: 100px !important;
  }
  
  .stat-cards {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 12px;
  }
  
  .chart-card {
    height: 300px;
  }
  
  .chart-wrapper {
    height: 220px;
  }
  
  .recent-activity {
    height: 200px;
  }
  
  .activity-list {
    max-height: 140px;
  }
  
  .quick-actions {
    height: 180px;
  }
}

@media (max-width: 1200px) {
  .dashboard-main {
    grid-template-columns: 1fr;
  }
  
  .side-section {
    flex-direction: row;
    gap: 16px;
  }
  
  .recent-activity, .quick-actions {
    flex: 1;
    height: 220px;
  }
  
  .activity-list {
    max-height: 140px;
  }
}
</style> 