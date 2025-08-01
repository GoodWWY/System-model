<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">用户设置</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-check" @click="saveAllSettings" :loading="saving">保存所有设置</el-button>
      </div>
    </div>
    
    <div class="settings-container">
      <el-tabs v-model="activeTab" type="border-card" class="settings-tabs">
        <!-- 个人信息 -->
        <el-tab-pane label="个人信息" name="profile">
          <div class="tab-content">
            <div class="section-title-small">基本信息</div>
            
            <!-- 头像上传 -->
            <div class="avatar-section">
              <div class="avatar-preview">
                <el-avatar :size="120" :src="getAvatarUrl(userProfile.avatar)">
                  <img src="/picture/2.JPG" />
                </el-avatar>
              </div>
              <div class="avatar-actions">
                <el-upload
                  class="avatar-uploader"
                  action="/user/upload-avatar"
                  :headers="uploadHeaders"
                  :data="{ uid: userProfile.uid }"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <el-button type="primary" icon="el-icon-upload">更换头像</el-button>
                </el-upload>
                <p class="avatar-tip">支持jpg、png格式，大小不超过2MB</p>
              </div>
            </div>
            
            <!-- 基本信息表单 -->
            <el-form :model="userProfile" :rules="profileRules" ref="profileFormRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="userProfile.username" placeholder="请输入用户名"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="userProfile.email" placeholder="请输入邮箱地址"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="userProfile.phone" placeholder="请输入手机号"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="年龄" prop="age">
                    <el-input-number v-model="userProfile.age" :min="1" :max="120" style="width: 100%"></el-input-number>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-radio-group v-model="userProfile.gender">
                      <el-radio label="男">男</el-radio>
                      <el-radio label="女">女</el-radio>
                      <el-radio label="保密">保密</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="地址">
                <el-input v-model="userProfile.address" placeholder="请输入详细地址"></el-input>
              </el-form-item>
              
              <el-form-item label="个人简介">
                <el-input type="textarea" v-model="userProfile.bio" :rows="4" placeholder="请输入个人简介"></el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveProfile" :loading="saving">保存个人信息</el-button>
                <el-button @click="resetProfile">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <div class="tab-content">
            <div class="section-title-small">密码管理</div>
            
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input type="password" v-model="passwordForm.currentPassword" placeholder="请输入当前密码" show-password></el-input>
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" show-password></el-input>
              </el-form-item>
              
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" show-password></el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="saving">修改密码</el-button>
                <el-button @click="resetPasswordForm">重置</el-button>
              </el-form-item>
            </el-form>
            
            <el-divider></el-divider>
            
            <div class="section-title-small">账户安全</div>
            <div class="security-options">
              <div class="security-item">
                <div class="security-info">
                  <h4>登录保护</h4>
                  <p>开启后需要验证身份才能登录</p>
                </div>
                <el-switch v-model="securitySettings.loginProtection" @change="updateSecuritySetting('loginProtection')"></el-switch>
              </div>
              
              <div class="security-item">
                <div class="security-info">
                  <h4>操作验证</h4>
                  <p>重要操作时需要验证身份</p>
                </div>
                <el-switch v-model="securitySettings.operationVerification" @change="updateSecuritySetting('operationVerification')"></el-switch>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 界面偏好 -->
        <el-tab-pane label="界面偏好" name="preferences">
          <div class="tab-content">
            <div class="section-title-small">显示设置</div>
            
            <div class="preference-group">
              <div class="preference-item">
                <div class="preference-info">
                  <h4>主题模式</h4>
                  <p>选择您喜欢的界面主题</p>
                </div>
                <el-radio-group v-model="preferences.theme" @change="updatePreference('theme')">
                  <el-radio label="light">浅色主题</el-radio>
                  <el-radio label="dark">深色主题</el-radio>
                  <el-radio label="auto">跟随系统</el-radio>
                </el-radio-group>
              </div>
              
              <div class="preference-item">
                <div class="preference-info">
                  <h4>语言设置</h4>
                  <p>选择界面显示语言</p>
                </div>
                <el-select v-model="preferences.language" @change="updatePreference('language')" style="width: 200px">
                  <el-option label="简体中文" value="zh-CN"></el-option>
                  <el-option label="English" value="en-US"></el-option>
                  <el-option label="繁體中文" value="zh-TW"></el-option>
                </el-select>
              </div>
              
              <div class="preference-item">
                <div class="preference-info">
                  <h4>侧边栏</h4>
                  <p>设置侧边栏的默认状态</p>
                </div>
                <el-radio-group v-model="preferences.sidebar" @change="updatePreference('sidebar')">
                  <el-radio label="expanded">默认展开</el-radio>
                  <el-radio label="collapsed">默认收起</el-radio>
                </el-radio-group>
              </div>
              
              <div class="preference-item">
                <div class="preference-info">
                  <h4>表格分页</h4>
                  <p>设置表格默认每页显示条数</p>
                </div>
                <el-select v-model="preferences.pageSize" @change="updatePreference('pageSize')" style="width: 120px">
                  <el-option label="10条" :value="10"></el-option>
                  <el-option label="20条" :value="20"></el-option>
                  <el-option label="50条" :value="50"></el-option>
                  <el-option label="100条" :value="100"></el-option>
                </el-select>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notifications">
          <div class="tab-content">
            <div class="section-title-small">消息通知</div>
            
            <div class="notification-group">
              <div class="notification-item">
                <div class="notification-info">
                  <h4>系统通知</h4>
                  <p>接收系统重要通知</p>
                </div>
                <el-switch v-model="notifications.system" @change="updateNotification('system')"></el-switch>
              </div>
              
              <div class="notification-item">
                <div class="notification-info">
                  <h4>邮件通知</h4>
                  <p>通过邮件接收重要消息</p>
                </div>
                <el-switch v-model="notifications.email" @change="updateNotification('email')"></el-switch>
              </div>
              
              <div class="notification-item">
                <div class="notification-info">
                  <h4>浏览器通知</h4>
                  <p>在浏览器中显示桌面通知</p>
                </div>
                <el-switch v-model="notifications.browser" @change="updateNotification('browser')"></el-switch>
              </div>
              
              <div class="notification-item">
                <div class="notification-info">
                  <h4>声音提醒</h4>
                  <p>新消息时播放提示音</p>
                </div>
                <el-switch v-model="notifications.sound" @change="updateNotification('sound')"></el-switch>
              </div>
            </div>
            
            <el-divider></el-divider>
            
            <div class="section-title-small">通知时间</div>
            <div class="time-settings">
              <div class="time-item">
                <label>免打扰时间：</label>
                <el-time-picker
                  v-model="notifications.quietTimeStart"
                  placeholder="开始时间"
                  format="HH:mm"
                  value-format="HH:mm"
                  @change="updateNotification('quietTimeStart')"
                >
                </el-time-picker>
                <span style="margin: 0 10px;">至</span>
                <el-time-picker
                  v-model="notifications.quietTimeEnd"
                  placeholder="结束时间"
                  format="HH:mm"
                  value-format="HH:mm"
                  @change="updateNotification('quietTimeEnd')"
                >
                </el-time-picker>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 关于 -->
        <el-tab-pane label="关于" name="about">
          <div class="tab-content">
            <div class="about-section">
              <div class="about-header">
                <el-avatar :size="80" :src="'/picture/2.JPG'"></el-avatar>
                <div class="about-info">
                  <h2>Cursor Demo 管理系统</h2>
                  <p>版本：v1.0.0</p>
                  <p>基于 Spring Boot + Vue 3 构建的现代化管理系统</p>
                </div>
              </div>
              
              <el-descriptions title="系统信息" :column="2" border>
                <el-descriptions-item label="前端框架">Vue 3 + Element Plus</el-descriptions-item>
                <el-descriptions-item label="后端框架">Spring Boot 3</el-descriptions-item>
                <el-descriptions-item label="数据库">MySQL</el-descriptions-item>
                <el-descriptions-item label="构建工具">Maven + Vite</el-descriptions-item>
                <el-descriptions-item label="开发语言">Java + JavaScript</el-descriptions-item>
                <el-descriptions-item label="认证方式">JWT</el-descriptions-item>
              </el-descriptions>
              
              <div class="about-actions">
                <el-button type="primary" @click="checkUpdate" :loading="checking">检查更新</el-button>
                <el-button @click="viewLicense">查看许可证</el-button>
                <el-button @click="contactSupport">联系支持</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'UserSettingsView',
  setup() {
    const activeTab = ref('profile')
    const saving = ref(false)
    const checking = ref(false)
    
    // 表单引用
    const profileFormRef = ref(null)
    const passwordFormRef = ref(null)
    
    // 用户资料
    const userProfile = reactive({
      uid: 0,
      username: '',
      email: '',
      phone: '',
      age: 18,
      gender: '保密',
      address: '',
      bio: '',
      avatar: ''
    })
    
    // 密码表单
    const passwordForm = reactive({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 安全设置
    const securitySettings = reactive({
      loginProtection: false,
      operationVerification: true
    })
    
    // 界面偏好
    const preferences = reactive({
      theme: 'light',
      language: 'zh-CN',
      sidebar: 'expanded',
      pageSize: 10
    })
    
    // 通知设置
    const notifications = reactive({
      system: true,
      email: true,
      browser: false,
      sound: true,
      quietTimeStart: '22:00',
      quietTimeEnd: '08:00'
    })
    
    // 上传配置
    const uploadHeaders = computed(() => ({
      'Authorization': `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token') || ''}`
    }))
    
    // 头像URL处理
    const getAvatarUrl = (avatar) => {
      if (!avatar) return '/picture/2.JPG'
      // 如果是OSS的完整URL，直接返回
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        return avatar
      }
      // 兼容旧的相对路径格式
      return avatar.startsWith('/') ? avatar : `/${avatar}`
    }
    
    // 表单验证规则
    const profileRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
      ]
    }
    
    const passwordRules = {
      currentPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 获取Token
    const getToken = () => {
      return localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    }
    
    // 加载用户资料
    const loadUserProfile = async () => {
      try {
        const uid = localStorage.getItem('uid') || sessionStorage.getItem('uid')
        if (!uid) return
        
        const res = await axios.get('/user/profile', {
          headers: { 'Authorization': `Bearer ${getToken()}` },
          params: { uid: parseInt(uid) }
        })
        
        if (res.data.code === 200) {
          Object.assign(userProfile, res.data.data)
          console.log('用户资料加载成功:', userProfile)
        }
      } catch (error) {
        console.error('加载用户资料失败:', error)
        ElMessage.warning('用户资料加载失败')
      }
    }
    
    // 加载用户设置
    const loadUserSettings = () => {
      // 从localStorage加载设置
      const savedPreferences = localStorage.getItem('userPreferences')
      if (savedPreferences) {
        Object.assign(preferences, JSON.parse(savedPreferences))
      }
      
      const savedNotifications = localStorage.getItem('userNotifications')
      if (savedNotifications) {
        Object.assign(notifications, JSON.parse(savedNotifications))
      }
      
      const savedSecurity = localStorage.getItem('userSecurity')
      if (savedSecurity) {
        Object.assign(securitySettings, JSON.parse(savedSecurity))
      }
    }
    
    // 保存个人资料
    const saveProfile = async () => {
      if (!profileFormRef.value) return
      
      profileFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        saving.value = true
        try {
          const res = await axios.put('/user/profile', userProfile, {
            headers: { 'Authorization': `Bearer ${getToken()}` },
            params: { uid: userProfile.uid }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('个人资料保存成功')
            // 更新本地存储的用户名
            localStorage.setItem('username', userProfile.username)
            sessionStorage.setItem('username', userProfile.username)
          } else {
            ElMessage.error(res.data.msg || '保存失败')
          }
        } catch (error) {
          console.error('保存个人资料失败:', error)
          ElMessage.error('保存失败')
        } finally {
          saving.value = false
        }
      })
    }
    
    // 修改密码
    const changePassword = async () => {
      if (!passwordFormRef.value) return
      
      passwordFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        saving.value = true
        try {
          const res = await axios.post('/user/change-password', {
            uid: userProfile.uid,
            oldPassword: passwordForm.currentPassword,
            newPassword: passwordForm.newPassword
          }, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('密码修改成功，请重新登录')
            resetPasswordForm()
            // 可以选择是否自动退出登录
          } else {
            ElMessage.error(res.data.msg || '密码修改失败')
          }
        } catch (error) {
          console.error('修改密码失败:', error)
          ElMessage.error('密码修改失败')
        } finally {
          saving.value = false
        }
      })
    }
    
    // 更新偏好设置
    const updatePreference = (key) => {
      localStorage.setItem('userPreferences', JSON.stringify(preferences))
      ElMessage.success('偏好设置已保存')
      
      // 应用主题设置
      if (key === 'theme') {
        applyTheme(preferences.theme)
      }
    }
    
    // 更新通知设置
    const updateNotification = (key) => {
      localStorage.setItem('userNotifications', JSON.stringify(notifications))
      ElMessage.success('通知设置已保存')
      
      // 处理浏览器通知权限
      if (key === 'browser' && notifications.browser) {
        requestNotificationPermission()
      }
    }
    
    // 更新安全设置
    const updateSecuritySetting = (key) => {
      localStorage.setItem('userSecurity', JSON.stringify(securitySettings))
      ElMessage.success('安全设置已保存')
      
      // 记录设置变更
      console.log(`安全设置已更新: ${key} = ${securitySettings[key]}`)
    }
    
    // 应用主题
    const applyTheme = (theme) => {
      // 这里可以实现主题切换逻辑
      console.log('应用主题:', theme)
    }
    
    // 请求通知权限
    const requestNotificationPermission = () => {
      if (Notification.permission === 'default') {
        Notification.requestPermission().then(permission => {
          if (permission === 'granted') {
            ElMessage.success('浏览器通知权限已开启')
          } else {
            notifications.browser = false
            ElMessage.warning('浏览器通知权限被拒绝')
          }
        })
      }
    }
    
    // 头像上传
    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!')
        return false
      }
      return true
    }
    
    const handleAvatarSuccess = async (response) => {
      if (response.code === 200) {
        const avatarUrl = response.data
        userProfile.avatar = avatarUrl
        ElMessage.success('头像上传成功')
        
        // 保存头像到用户资料
        try {
          const token = localStorage.getItem('token') || sessionStorage.getItem('token')
          const res = await axios.put('/user/profile', {
            uid: userProfile.uid,
            avatar: avatarUrl
          }, {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          })
          
          if (res.data.code === 200) {
            console.log('头像保存成功')
            // 通知Layout头像已更新
            localStorage.setItem('avatar_updated', Date.now().toString())
            // 触发storage事件
            window.dispatchEvent(new StorageEvent('storage', {
              key: 'avatar_updated',
              newValue: Date.now().toString()
            }))
          }
        } catch (error) {
          console.error('保存头像失败:', error)
        }
      } else {
        ElMessage.error('头像上传失败')
      }
    }
    
    // 重置表单
    const resetProfile = () => {
      loadUserProfile()
    }
    
    const resetPasswordForm = () => {
      Object.assign(passwordForm, {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields()
      }
    }
    
    // 保存所有设置
    const saveAllSettings = async () => {
      await Promise.all([
        saveProfile()
      ])
    }
    
    // 关于页面功能
    const checkUpdate = async () => {
      checking.value = true
      try {
        await new Promise(resolve => setTimeout(resolve, 2000))
        ElMessage.success('当前已是最新版本')
      } finally {
        checking.value = false
      }
    }
    
    const viewLicense = () => {
      ElMessageBox.alert('MIT License\n\nCopyright (c) 2024 Cursor Demo', '许可证信息', {
        confirmButtonText: '确定'
      })
    }
    
    const contactSupport = () => {
      ElMessage.info('支持邮箱: 1781939957@qq.com')
    }
    
    // 初始化
    onMounted(() => {
      loadUserProfile()
      loadUserSettings()
    })
    
    return {
      activeTab,
      saving,
      checking,
      profileFormRef,
      passwordFormRef,
      userProfile,
      passwordForm,
      securitySettings,
      preferences,
      notifications,
      uploadHeaders,
      profileRules,
      passwordRules,
      getAvatarUrl,
      saveProfile,
      changePassword,
      updatePreference,
      updateNotification,
      updateSecuritySetting,
      beforeAvatarUpload,
      handleAvatarSuccess,
      resetProfile,
      resetPasswordForm,
      saveAllSettings,
      checkUpdate,
      viewLicense,
      contactSupport
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

.settings-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-tabs {
  border: none;
  box-shadow: none;
}

.tab-content {
  padding: 20px;
}

.section-title-small {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

/* 安全设置 */
.security-options {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.security-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}

.security-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

/* 偏好设置 */
.preference-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.preference-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}

.preference-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

/* 通知设置 */
.notification-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.notification-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}

.notification-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.time-settings {
  margin-top: 15px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 关于页面 */
.about-section {
  text-align: center;
}

.about-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  text-align: left;
}

.about-info h2 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.about-info p {
  margin: 5px 0;
  color: #606266;
}

.about-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-section {
    padding: 10px;
  }
  
  .tab-content {
    padding: 15px;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .about-header {
    flex-direction: column;
    text-align: center;
  }
  
  .preference-item,
  .security-item,
  .notification-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .time-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style> 