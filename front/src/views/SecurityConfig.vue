<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">安全配置</h2>
    </div>
    <div class="placeholder-container">
      <el-empty description="安全配置功能开发中" :image-size="100">
        <template #image>
          <el-icon :size="100" color="#909399">
            <Shield />
          </el-icon>
        </template>
        <el-button type="primary" @click="goBack">返回仪表盘</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'SecurityConfigView',
  setup() {
    const router = useRouter()
    
    const goBack = () => {
      router.push('/dashboard')
    }
    
    return {
      goBack
    }
  }
}
</script>

<style scoped>
.content-section {
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.placeholder-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style> 