<template>
  <div class="personal-center">
    <el-tabs v-model="activeTab" class="center-tabs">
      <el-tab-pane label="个人信息" name="profile">
        <div class="profile-container">
          <div class="avatar-section">
            <div class="avatar-upload">
              <div class="avatar-preview">
                <el-avatar 
                  :size="120" 
                  :src="getAvatarUrl(userProfile.avatar)" 
                  @error="() => true"
                >
                  <img src="/picture/2.JPG" />
                </el-avatar>
              </div>
              <el-upload
                class="avatar-uploader"
                action="/user/upload-avatar"
                :headers="uploadHeaders"
                :data="{ uid: userProfile.uid }"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-button type="primary" size="small">更换头像</el-button>
              </el-upload>
            </div>
          </div>
          
          <div class="form-section">
            <el-form :model="userProfile" label-width="100px" :rules="profileRules" ref="profileFormRef">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="userProfile.username"></el-input>
              </el-form-item>
              
              <el-form-item label="性别">
                <el-radio-group v-model="userProfile.gender">
                  <el-radio label="男">男</el-radio>
                  <el-radio label="女">女</el-radio>
                  <el-radio label="保密">保密</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="userProfile.email"></el-input>
              </el-form-item>
              
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="userProfile.phone"></el-input>
              </el-form-item>
              
              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="userProfile.age" :min="1" :max="120"></el-input-number>
              </el-form-item>
              
              <el-form-item label="地址">
                <el-input v-model="userProfile.address"></el-input>
              </el-form-item>
              
              <el-form-item label="个人简介">
                <el-input type="textarea" v-model="userProfile.bio" :rows="4"></el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存信息</el-button>
                <el-button @click="resetProfile">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="修改密码" name="password">
        <div class="password-container">
          <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'PersonalCenter',
  setup() {
    const route = useRoute()
    const activeTab = ref('profile')
    const profileFormRef = ref(null)
    const passwordFormRef = ref(null)
    
    const userProfile = reactive({
      uid: parseInt(localStorage.getItem('uid') || sessionStorage.getItem('uid') || '0'),
      username: '',
      gender: '保密',
      email: '',
      avatar: '',
      phone: '',
      address: '',
      age: 18,
      bio: ''
    })
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const profileRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { pattern: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ]
    }
    
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
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
    
    // 上传头像的请求头
    const uploadHeaders = computed(() => {
      const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
      return {
        Authorization: `Bearer ${token}`
      }
    })
    
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
    
    // 获取用户信息
    const fetchUserProfile = async () => {
      try {
        const uid = userProfile.uid
        if (!uid) return
        
        console.log('正在获取用户信息:', uid)
        
        const res = await axios.get(`/user/profile?uid=${uid}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token') || ''}`
          }
        })
        
        console.log('获取用户信息响应:', res.data)
        
        if (res.data.code === 200) {
          const data = res.data.data
          userProfile.username = data.username || ''
          userProfile.gender = data.gender || '保密'
          userProfile.email = data.email || ''
          userProfile.avatar = data.avatar || ''
          userProfile.phone = data.phone || ''
          userProfile.address = data.address === null ? '' : data.address
          userProfile.age = data.age || 18
          userProfile.bio = data.bio === null ? '' : data.bio
          
          console.log('处理后的用户信息:', JSON.stringify(userProfile))
        } else {
          ElMessage.error('获取用户信息失败')
        }
      } catch (error) {
        console.error('获取用户信息失败', error)
        ElMessage.error('获取用户信息失败')
      }
    }
    
    // 头像上传前的验证
    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isImage) {
        ElMessage.error('上传头像图片只能是图片格式!')
        return false
      }
      
      if (!isLt2M) {
        ElMessage.error('上传头像图片大小不能超过 2MB!')
        return false
      }
      
      return true
    }
    
    // 头像上传成功的回调
    const handleAvatarSuccess = async (res) => {
      console.log('头像上传响应:', res)
      try {
        if (res.code === 200) {
          const avatarUrl = res.data
          
          // 等待一段时间，确保文件已经写入磁盘
          await new Promise(resolve => setTimeout(resolve, 500))
          
          // 更新个人资料中的头像
          userProfile.avatar = avatarUrl
          console.log('更新头像URL:', userProfile.avatar)
          ElMessage.success('头像上传成功')
          
          // 强制刷新头像显示
          const avatarElements = document.querySelectorAll('.el-avatar img')
          avatarElements.forEach(img => {
            if (img.src.includes('/uploads/avatars/')) {
              img.src = getAvatarUrl(avatarUrl)
            }
          })
          
          // 保存用户信息以确保头像更新
          saveUserAvatar(avatarUrl)
        } else {
          console.error('头像上传失败:', res)
          ElMessage.error(res.msg || '头像上传失败')
        }
      } catch (error) {
        console.error('处理头像上传响应时出错:', error)
        ElMessage.error('处理头像上传响应时出错')
      }
    }
    
    // 保存用户头像
    const saveUserAvatar = async (avatarUrl) => {
      try {
        const profileData = {
          uid: userProfile.uid,
          avatar: avatarUrl
        }
        
        console.log('保存用户头像:', JSON.stringify(profileData))
        
        const res = await axios.put(`/user/profile?uid=${profileData.uid}`, profileData, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token') || ''}`,
            'Content-Type': 'application/json'
          }
        })
        
        console.log('保存用户头像响应:', res.data)
        
        if (res.data.code === 200) {
          console.log('头像保存成功')
          // 重新获取用户信息刷新页面显示
          await fetchUserProfile()
          // 通知Layout头像已更新
          localStorage.setItem('avatar_updated', Date.now().toString())
          // 触发storage事件（对于同一页面）
          window.dispatchEvent(new StorageEvent('storage', {
            key: 'avatar_updated',
            newValue: Date.now().toString()
          }))
          // 通知用户更新成功
          ElMessage.success('头像更新成功')
        } else {
          console.error('头像保存失败:', res.data)
        }
      } catch (error) {
        console.error('保存用户头像失败:', error)
      }
    }
    
    // 保存用户信息
    const saveProfile = async () => {
      if (!profileFormRef.value) return
      
      profileFormRef.value.validate(async valid => {
        if (!valid) return
        
        try {
          const profileData = {
            uid: userProfile.uid,
            username: userProfile.username || '',
            gender: userProfile.gender || '保密',
            email: userProfile.email || '',
            avatar: userProfile.avatar || '',
            phone: userProfile.phone || '',
            address: userProfile.address !== undefined ? userProfile.address : '',
            age: userProfile.age || 18,
            bio: userProfile.bio !== undefined ? userProfile.bio : ''
          }
          
          console.log('正在保存用户信息:', JSON.stringify(profileData))
          
          const res = await axios.put(`/user/profile?uid=${profileData.uid}`, profileData, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token') || ''}`,
              'Content-Type': 'application/json'
            }
          })
          
          console.log('保存用户信息响应:', res.data)
          
          if (res.data.code === 200) {
            ElMessage.success('保存成功')
            
            // 更新本地存储的用户名
            localStorage.setItem('username', profileData.username)
            sessionStorage.setItem('username', profileData.username)
            
            // 保存成功后重新获取用户信息
            fetchUserProfile()
          } else {
            ElMessage.error(res.data.msg || '保存失败')
          }
        } catch (error) {
          console.error('保存用户信息失败', error)
          console.error('错误详情:', error.response ? error.response.data : '无响应数据')
          ElMessage.error('保存用户信息失败: ' + (error.message || '未知错误'))
        }
      })
    }
    
    // 重置个人信息表单
    const resetProfile = () => {
      fetchUserProfile()
    }
    
    // 修改密码
    const changePassword = async () => {
      if (!passwordFormRef.value) return
      
      passwordFormRef.value.validate(async valid => {
        if (!valid) return
        
        try {
          const formData = new FormData()
          formData.append('uid', userProfile.uid)
          formData.append('oldPassword', passwordForm.oldPassword)
          formData.append('newPassword', passwordForm.newPassword)
          
          const res = await axios.post('/user/change-password', formData, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token') || ''}`
            }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('密码修改成功')
            resetPasswordForm()
          } else {
            ElMessage.error(res.data.msg || '密码修改失败')
          }
        } catch (error) {
          console.error('修改密码失败', error)
          ElMessage.error('修改密码失败')
        }
      })
    }
    
    // 重置密码表单
    const resetPasswordForm = () => {
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields()
      }
    }
    
    onMounted(() => {
      // 检查URL参数，如果有tab参数则切换到对应标签页
      if (route.query.tab === 'password') {
        activeTab.value = 'password'
      }
      
      fetchUserProfile()
    })
    
    return {
      activeTab,
      profileFormRef,
      passwordFormRef,
      userProfile,
      passwordForm,
      profileRules,
      passwordRules,
      uploadHeaders,
      getAvatarUrl,
      fetchUserProfile,
      beforeAvatarUpload,
      handleAvatarSuccess,
      saveProfile,
      resetProfile,
      changePassword,
      resetPasswordForm
    }
  }
}
</script>

<style scoped>
.personal-center {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.center-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.profile-container {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 40px;
  align-items: start;
}

.avatar-section {
  display: flex;
  justify-content: center;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar-preview {
  position: relative;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.form-section {
  width: 100%;
}

.password-container {
  max-width: 500px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style> 