<template>
  <div class="dashboard">
    <h2>仪表板</h2>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <h3>总用户数</h3>
        <p class="stat-number">{{ stats.totalUsers }}</p>
      </div>
      <div class="stat-card">
        <h3>今日新增</h3>
        <p class="stat-number">{{ stats.todayUsers }}</p>
      </div>
      <div class="stat-card">
        <h3>活跃用户</h3>
        <p class="stat-number">{{ stats.activeUsers }}</p>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-container" v-if="showChart">
      <h3>用户增长趋势</h3>
      <div ref="chartRef" style="width: 100%; height: 400px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

// 响应式数据
const stats = ref({
  totalUsers: 0,
  todayUsers: 0,
  activeUsers: 0
})

const chartRef = ref(null)
const chartInstance = ref(null)
const showChart = ref(false)

// 获取统计数据
const fetchStats = async () => {
  try {
    // 这里可以调用API获取真实数据
    // const response = await fetch('/api/stats')
    // const data = await response.json()
    
    // 模拟数据
    stats.value = {
      totalUsers: 1250,
      todayUsers: 45,
      activeUsers: 892
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化图表
const initChart = async () => {
  await nextTick()
  
  if (!chartRef.value) {
    console.error('图表容器未找到')
    return
  }

  try {
    // 确保ECharts正确初始化
    if (chartInstance.value) {
      chartInstance.value.dispose()
    }

    chartInstance.value = echarts.init(chartRef.value)
    
    // 图表配置
    const option = {
      title: {
        text: '用户增长趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        formatter: function (params) {
          if (params && params.length > 0) {
            return `${params[0].name}<br/>${params[0].seriesName}: ${params[0].value}`
          }
          return ''
        }
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
      },
      yAxis: {
        type: 'value',
        name: '用户数量'
      },
      series: [
        {
          name: '新增用户',
          type: 'line', // 确保指定type属性
          data: [120, 132, 101, 134, 90, 230, 210],
          smooth: true,
          itemStyle: {
            color: '#409EFF'
          },
          lineStyle: {
            color: '#409EFF'
          }
        }
      ]
    }

    // 设置图表配置
    chartInstance.value.setOption(option)
    
    // 监听窗口大小变化
    const resizeHandler = () => {
      if (chartInstance.value) {
        chartInstance.value.resize()
      }
    }
    window.addEventListener('resize', resizeHandler)
    
    console.log('图表初始化成功')
    showChart.value = true
    
    return resizeHandler
  } catch (error) {
    console.error('图表初始化失败:', error)
    showChart.value = false
  }
}

// 生命周期
onMounted(async () => {
  await fetchStats()
  await initChart()
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.stat-number {
  margin: 0;
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.chart-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chart-container h3 {
  margin: 0 0 20px 0;
  color: #333;
}
</style> 