<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-content">
        <!-- 左侧品牌区域 -->
        <div class="brand-section">
          <div class="brand-content">
            <div class="logo">
              <i class="el-icon-monitor"></i>
            </div>
            <h1 class="brand-title">Cursor Demo</h1>
            <p class="brand-desc">欢迎使用我们的系统，高效管理，智能分析</p>
            <div class="decoration-circles">
              <div class="circle circle-1"></div>
              <div class="circle circle-2"></div>
              <div class="circle circle-3"></div>
            </div>
          </div>
        </div>
        
        <!-- 右侧登录表单 -->
        <div class="form-section">
          <div class="form-header">
            <h2 class="welcome-text">欢迎回来</h2>
            <p class="subtitle">请登录您的账号</p>
          </div>
          
          <el-form 
            :model="form" 
            ref="loginForm" 
            :rules="rules" 
            class="login-form"
            label-position="top"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入用户名" 
                autocomplete="off" 
                size="large" 
                clearable 
              />
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码" 
                autocomplete="off" 
                size="large" 
                clearable 
                show-password
              />
            </el-form-item>
            
            <el-form-item class="slider-container">
              <div class="slider-verify" :class="{'success-verify': sliderSuccess}">
                <div class="slider-bg" :style="{width: sliderX + 'px'}"></div>
                <div 
                  class="slider-btn" 
                  :class="{'success': sliderSuccess}"
                  :style="{left: sliderX + 'px'}" 
                  @mousedown="startDrag" 
                  @touchstart="startDrag"
                >
                  <span v-if="!sliderSuccess">→</span>
                  <span v-else>✓</span>
                </div>
                <span class="slider-text">{{ sliderSuccess ? '验证通过' : '请向右滑动验证' }}</span>
              </div>
            </el-form-item>
            
            <el-form-item class="btn-container">
              <el-button 
                type="primary" 
                :loading="loading"
                :disabled="!sliderSuccess || loading" 
                @click="onLogin" 
                class="login-btn"
              >
                登录
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <el-button type="text" @click="goRegister" class="register-link">
                没有账号？立即注册
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter()
    const loginForm = ref(null)
    const form = reactive({ username: '', password: '' })
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
      ]
    }
    const loading = ref(false)
    // 滑块验证相关
    const sliderX = ref(0)
    const sliderSuccess = ref(false)
    let startX = 0
    let dragging = false
    const sliderContainer = ref(null)
    const maxSlider = ref(220)
    
    // 在组件挂载后获取滑块容器的实际宽度
    onMounted(() => {
      const container = document.querySelector('.slider-verify');
      if (container) {
        // 滑块宽度应该是容器宽度减去滑块按钮的宽度
        maxSlider.value = container.clientWidth - 40;
      }
    })

    const startDrag = (e) => {
      if (sliderSuccess.value) return
      dragging = true
      startX = e.type === 'touchstart' ? e.touches[0].clientX : e.clientX
      document.addEventListener('mousemove', onDrag)
      document.addEventListener('mouseup', endDrag)
      document.addEventListener('touchmove', onDrag)
      document.addEventListener('touchend', endDrag)
    }
    
    const onDrag = (e) => {
      if (!dragging) return
      const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
      let moveX = clientX - startX
      if (moveX < 0) moveX = 0
      if (moveX > maxSlider.value) moveX = maxSlider.value
      sliderX.value = moveX
      
      // 只有当滑块滑到最右侧时才验证通过
      if (moveX >= maxSlider.value - 2) { // 允许2px的误差
        sliderSuccess.value = true
        sliderX.value = maxSlider.value // 确保滑块到达最右侧
        dragging = false
        document.removeEventListener('mousemove', onDrag)
        document.removeEventListener('mouseup', endDrag)
        document.removeEventListener('touchmove', onDrag)
        document.removeEventListener('touchend', endDrag)
      }
    }
    
    const endDrag = () => {
      if (!sliderSuccess.value) {
        // 添加动画效果
        const resetAnimation = () => {
          const step = Math.max(5, sliderX.value / 10);
          sliderX.value -= step;
          if (sliderX.value > 0) {
            requestAnimationFrame(resetAnimation);
          } else {
            sliderX.value = 0;
          }
        };
        requestAnimationFrame(resetAnimation);
      }
      dragging = false
      document.removeEventListener('mousemove', onDrag)
      document.removeEventListener('mouseup', endDrag)
      document.removeEventListener('touchmove', onDrag)
      document.removeEventListener('touchend', endDrag)
    }
    
    // 登录逻辑
    const onLogin = () => {
      // 先执行表单验证
      if (!sliderSuccess.value) {
        ElMessage.error('请完成滑动验证')
        return
      }
      
      loginForm.value.validate(async valid => {
        if (!valid) return
        
        loading.value = true
        try {
          const res = await axios.post('/user/login', form)
          const data = res.data
          if (data.code === 200) {
            ElMessage.success('登录成功')
            
            // 存储token和用户信息
            localStorage.setItem('token', data.data.token)
            localStorage.setItem('username', form.username)
            
            // 重要：存储用户ID，用于获取头像
            if (data.data.uid !== undefined && data.data.uid !== null) {
              console.log('获取到用户ID:', data.data.uid)
              localStorage.setItem('uid', data.data.uid)
              sessionStorage.setItem('uid', data.data.uid)
            } else {
              console.error('登录响应中没有uid值:', data.data)
            }
            
            // 同时也存储到sessionStorage，以便与其他组件兼容
            sessionStorage.setItem('token', data.data.token)
            sessionStorage.setItem('username', form.username)
            
            console.log('登录成功，用户信息：', data.data)
            
            // 如果有重定向参数，跳转到对应页面
            const redirect = router.currentRoute.value.query.redirect || '/home'
            router.push(redirect)
          } else {
            ElMessage.error(data.msg || '登录失败')
          }
        } catch (e) {
          console.error('登录请求失败:', e)
          ElMessage.error('网络错误')
        } finally {
          loading.value = false
        }
      })
    }
    
    // 在setup返回对象中添加goRegister方法
    const goRegister = () => {
      router.push('/register')
    }
    
    return { 
      form, 
      rules, 
      loginForm, 
      loading, 
      onLogin, 
      sliderX, 
      sliderSuccess, 
      startDrag, 
      goRegister,
      sliderContainer,
      maxSlider
    }
  }
}
</script>

<style scoped>
/* 基础样式 */
.login-page {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.login-container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.login-content {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(50, 50, 93, 0.1), 0 5px 15px rgba(0, 0, 0, 0.07);
  min-height: 600px;
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: white;
  padding: 40px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.logo {
  font-size: 3rem;
  margin-bottom: 20px;
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 1.1rem;
  opacity: 0.9;
  line-height: 1.6;
  margin-bottom: 30px;
}

/* 装饰圆圈 */
.decoration-circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 50px;
  left: -50px;
}

.circle-3 {
  width: 100px;
  height: 100px;
  bottom: 120px;
  left: 30%;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 30px;
  text-align: center;
}

.welcome-text {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.subtitle {
  color: #666;
  font-size: 1rem;
}

.login-form {
  width: 100%;
}

/* 表单元素样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
  padding-bottom: 8px;
}

:deep(.el-input__wrapper) {
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  padding: 0 15px;
  height: 50px;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  border-color: #6a11cb;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #6a11cb;
  box-shadow: 0 0 0 2px rgba(106, 17, 203, 0.2) !important;
}

:deep(.el-input__inner) {
  height: 48px;
}

/* 滑块验证样式 */
.slider-container {
  margin-top: 20px;
  margin-bottom: 20px;
}

.slider-verify {
  position: relative;
  width: 100%;
  height: 50px;
  background: #f5f7fa;
  border-radius: 25px;
  margin: 0 auto;
  user-select: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e4e7ed;
  overflow: hidden;
  transition: all 0.3s ease;
}

.slider-verify.success-verify {
  border-color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.slider-bg {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #6a11cb 0%, #2575fc 100%);
  border-radius: 25px;
  transition: width 0.3s ease;
  z-index: 1;
}

.slider-btn {
  position: absolute;
  top: 5px;
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  z-index: 2;
  transition: all 0.3s ease;
  border: none;
  color: #6a11cb;
}

.slider-btn.success {
  background: #67c23a;
  color: white;
}

.slider-text {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
  font-size: 14px;
  z-index: 1;
  letter-spacing: 1px;
}

/* 按钮样式 */
.btn-container {
  margin-top: 10px;
}

.login-btn {
  width: 100%;
  height: 50px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 7px 14px rgba(50, 50, 93, 0.1), 0 3px 6px rgba(0, 0, 0, 0.08);
}

.login-btn:active:not(:disabled) {
  transform: translateY(1px);
}

/* 底部链接 */
.form-footer {
  margin-top: 20px;
  text-align: center;
}

.register-link {
  color: #6a11cb;
  font-weight: 500;
  font-size: 0.95rem;
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-content {
    flex-direction: column;
  }
  
  .brand-section {
    padding: 30px;
    min-height: 200px;
  }
  
  .form-section {
    padding: 30px;
  }
  
  .brand-title {
    font-size: 2rem;
  }
}
</style>
<style>
html, body, #app {
  height: 100%;
  margin: 0 !important;
  padding: 0 !important;
}
</style> 