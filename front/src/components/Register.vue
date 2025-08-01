<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-content">
        <!-- 左侧品牌区域 -->
        <div class="brand-section">
          <div class="brand-content">
            <div class="logo">
              <i class="el-icon-monitor"></i>
            </div>
            <h1 class="brand-title">Cursor Demo</h1>
            <p class="brand-desc">加入我们，开启智能管理新体验</p>
            <div class="decoration-circles">
              <div class="circle circle-1"></div>
              <div class="circle circle-2"></div>
              <div class="circle circle-3"></div>
            </div>
          </div>
        </div>
        
        <!-- 右侧注册表单 -->
        <div class="form-section">
          <div class="form-header">
            <h2 class="welcome-text">创建账号</h2>
            <p class="subtitle">请填写以下信息完成注册</p>
          </div>
          
          <el-form 
            :model="form" 
            ref="registerForm" 
            :rules="rules" 
            class="register-form"
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

            <el-form-item label="邮箱" prop="email">
              <el-input 
                v-model="form.email" 
                placeholder="请输入邮箱地址" 
                autocomplete="off" 
                size="large" 
                clearable 
              />
            </el-form-item>

            <el-form-item label="邮箱验证码" prop="verificationCode">
              <div class="verification-input">
                <el-input 
                  v-model="form.verificationCode" 
                  placeholder="请输入6位验证码" 
                  autocomplete="off" 
                  size="large" 
                  clearable 
                  maxlength="6"
                  style="flex: 1; margin-right: 10px"
                />
                <el-button 
                  type="primary" 
                  size="large"
                  :loading="sendingCode"
                  :disabled="!canSendCode || countdown > 0"
                  @click="sendVerificationCode"
                  class="send-code-btn"
                >
                  {{ countdown > 0 ? `${countdown}s后重试` : '发送验证码' }}
                </el-button>
              </div>
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
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="请再次输入密码" 
                autocomplete="off" 
                size="large" 
                clearable 
                show-password
              />
            </el-form-item>
            
            <el-form-item class="btn-container">
              <el-button 
                type="primary" 
                :loading="loading"
                @click="onRegister" 
                class="register-btn"
              >
                注册
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <el-button type="text" @click="goLogin" class="login-link">
                已有账号？立即登录
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'RegisterPage',
  setup() {
    const router = useRouter()
    const registerForm = ref(null)
    const form = reactive({ 
      username: '', 
      email: '',
      verificationCode: '',
      password: '',
      confirmPassword: ''
    })

    // 验证码相关状态
    const sendingCode = ref(false)
    const countdown = ref(0)
    const countdownTimer = ref(null)

    // 计算属性：是否可以发送验证码
    const canSendCode = computed(() => {
      return form.email && isValidEmail(form.email) && countdown.value === 0
    })

    // 邮箱格式验证
    const isValidEmail = (email) => {
      const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/
      return emailRegex.test(email)
    }

    // 开始倒计时
    const startCountdown = () => {
      countdown.value = 60
      countdownTimer.value = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer.value)
          countdownTimer.value = null
        }
      }, 1000)
    }

    // 发送验证码
    const sendVerificationCode = async () => {
      // 验证邮箱格式
      if (!form.email) {
        ElMessage.error('请先输入邮箱地址')
        return
      }
      
      if (!isValidEmail(form.email)) {
        ElMessage.error('请输入正确的邮箱格式')
        return
      }

      sendingCode.value = true
      try {
        const response = await axios.post('/user/send-register-code', null, {
          params: { email: form.email }
        })
        
        if (response.data.code === 200) {
          ElMessage.success(response.data.msg || '验证码发送成功，请查收邮件')
          startCountdown()
        } else {
          ElMessage.error(response.data.msg || '验证码发送失败')
        }
      } catch (error) {
        console.error('发送验证码失败:', error)
        if (error.response?.data?.msg) {
          ElMessage.error(error.response.data.msg)
        } else {
          ElMessage.error('验证码发送失败，请稍后重试')
        }
      } finally {
        sendingCode.value = false
      }
    }
    
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (form.confirmPassword !== '') {
          registerForm.value.validateField('confirmPassword')
        }
        callback()
      }
    }
    
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== form.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const validateEmail = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入邮箱地址'))
      } else if (!isValidEmail(value)) {
        callback(new Error('请输入正确的邮箱格式'))
      } else {
        callback()
      }
    }

    const validateVerificationCode = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入验证码'))
      } else if (!/^\d{6}$/.test(value)) {
        callback(new Error('验证码应为6位数字'))
      } else {
        callback()
      }
    }
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { validator: validateEmail, trigger: 'blur' }
      ],
      verificationCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { validator: validateVerificationCode, trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' },
        { validator: validatePass, trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePass2, trigger: 'blur' }
      ]
    }
    
    const loading = ref(false)
    
    const onRegister = async () => {
      if (!registerForm.value) return
      
      await registerForm.value.validate(async (valid) => {
        if (!valid) {
          return ElMessage.error('请正确填写表单')
        }
        
        loading.value = true
        try {
          const res = await axios.post('/user/register', {
            username: form.username,
            email: form.email,
            verificationCode: form.verificationCode,
            password: form.password
          })
          const data = res.data
          if (data.code === 200) {
            ElMessage.success(data.msg || '注册成功，请登录')
            router.push('/login')
          } else {
            ElMessage.error(data.msg || '注册失败')
          }
        } catch (e) {
          console.error('注册失败:', e)
          if (e.response?.data?.msg) {
            ElMessage.error(e.response.data.msg)
          } else {
            ElMessage.error('网络错误，请稍后重试')
          }
        } finally {
          loading.value = false
        }
      })
    }
    
    const goLogin = () => {
      router.push('/login')
    }

    // 组件销毁时清理定时器
    const cleanup = () => {
      if (countdownTimer.value) {
        clearInterval(countdownTimer.value)
        countdownTimer.value = null
      }
    }

    // 组件卸载时清理定时器
    onUnmounted(cleanup)
    
    return { 
      form, 
      rules, 
      registerForm,
      loading,
      sendingCode,
      countdown,
      canSendCode,
      sendVerificationCode,
      onRegister, 
      goLogin 
    }
  }
}
</script>

<style scoped>
/* 基础样式 */
.register-page {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.register-container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.register-content {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(50, 50, 93, 0.1), 0 5px 15px rgba(0, 0, 0, 0.07);
  min-height: 700px;
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

.register-form {
  width: 100%;
}

/* 验证码输入区域 */
.verification-input {
  display: flex;
  align-items: center;
  width: 100%;
}

.send-code-btn {
  flex-shrink: 0;
  width: 120px;
  height: 50px;
  border-radius: 10px;
  font-size: 14px;
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

/* 按钮样式 */
.btn-container {
  margin-top: 20px;
}

.register-btn {
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

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 7px 14px rgba(50, 50, 93, 0.1), 0 3px 6px rgba(0, 0, 0, 0.08);
}

.register-btn:active:not(:disabled) {
  transform: translateY(1px);
}

/* 底部链接 */
.form-footer {
  margin-top: 20px;
  text-align: center;
}

.login-link {
  color: #6a11cb;
  font-weight: 500;
  font-size: 0.95rem;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-content {
    flex-direction: column;
    min-height: auto;
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

  .verification-input {
    flex-direction: column;
    gap: 10px;
  }

  .send-code-btn {
    width: 100%;
  }
}
</style> 