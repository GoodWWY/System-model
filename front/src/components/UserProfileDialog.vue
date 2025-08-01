<template>
  <el-dialog v-model="dialogVisible" :title="editable ? '编辑用户' : '用户详情'" width="600px">
    <div class="profile-container">
      <div class="avatar-upload" v-if="editable">
        <div class="avatar-preview">
          <el-avatar 
            :size="100" 
            :src="getAvatarUrl(localUserProfile.avatar)" 
            @error="handleAvatarError"
          >
            <el-icon><User /></el-icon>
          </el-avatar>
        </div>
        <el-upload
          class="avatar-uploader"
          action="/user/upload-avatar"
          :headers="uploadHeaders"
          :data="{ uid: localUserProfile.uid }"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <el-button type="primary" size="small">更换头像</el-button>
        </el-upload>
      </div>
      
      <div class="avatar-view" v-else>
        <el-avatar 
          :size="100" 
          :src="getAvatarUrl(localUserProfile.avatar)" 
          @error="handleAvatarError"
        >
          <el-icon><User /></el-icon>
        </el-avatar>
      </div>
      
      <el-form :model="localUserProfile" label-width="80px" :rules="profileRules" ref="profileFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="localUserProfile.username" :disabled="!editable"></el-input>
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="localUserProfile.gender" :disabled="!editable">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
            <el-radio label="保密">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="localUserProfile.email" :disabled="!editable"></el-input>
        </el-form-item>
        
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="localUserProfile.phone" :disabled="!editable"></el-input>
        </el-form-item>
        
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="localUserProfile.age" :min="1" :max="120" :disabled="!editable"></el-input-number>
        </el-form-item>
        
        <el-form-item label="地址">
          <el-input v-model="localUserProfile.address" :disabled="!editable"></el-input>
        </el-form-item>
        
        <el-form-item label="个人简介">
          <el-input type="textarea" v-model="localUserProfile.bio" :rows="3" :disabled="!editable"></el-input>
        </el-form-item>
        
        <el-form-item label="用户状态" v-if="editable">
          <el-switch 
            v-model="localUserProfile.status" 
            active-text="正常" 
            inactive-text="禁用"
            active-value="active"
            inactive-value="inactive"
            :disabled="!editable"
          ></el-switch>
        </el-form-item>
        
        <el-form-item label="用户状态" v-else>
          <el-tag 
            :type="localUserProfile.status === 'active' ? 'success' : 'danger'"
            size="small"
          >
            {{ localUserProfile.status === 'active' ? '正常' : '禁用' }}
          </el-tag>
        </el-form-item>
        
        <el-form-item label="用户角色" v-if="editable">
          <el-select 
            v-model="localUserProfile.roleIds" 
            multiple 
            placeholder="请选择角色" 
            style="width: 100%"
            :disabled="!editable"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="用户角色" v-else>
          <div class="role-tags">
            <el-tag 
              v-for="role in userRoles" 
              :key="role.id" 
              :type="role.name.includes('管理员') ? 'danger' : 'info'"
              size="small"
              style="margin-right: 8px; margin-bottom: 4px;"
            >
              {{ role.name }}
            </el-tag>
            <span v-if="userRoles.length === 0" style="color: #999;">暂无角色</span>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div style="text-align: right;">
        <el-button @click="dialogVisible = false">{{ editable ? '取消' : '关闭' }}</el-button>
        <el-button v-if="editable" type="primary" @click="saveProfile">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, computed, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'UserProfileDialog',
  components: {
    User
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userProfile: {
      type: Object,
      default: () => ({})
    },
    editable: {
      type: Boolean,
      default: false
    },
    roleOptions: {
      type: Array,
      default: () => []
    },
    userRoles: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:visible', 'success'],
  setup(props, { emit }) {
    const dialogVisible = computed({
      get: () => props.visible,
      set: (value) => emit('update:visible', value)
    })
    
    const profileFormRef = ref(null)
    
    // 使用本地响应式数据，避免直接修改props
    const localUserProfile = reactive({
      uid: 0,
      username: '',
      gender: '保密',
      email: '',
      avatar: '',
      phone: '',
      address: '',
      age: 18,
      bio: '',
      status: 'active',
      roleIds: []
    })
    
    // 监听props变化，同步到本地数据
    watch(() => props.userProfile, (newProfile) => {
      if (newProfile) {
        Object.assign(localUserProfile, {
          uid: newProfile.uid || 0,
          username: newProfile.username || '',
          gender: newProfile.gender || '保密',
          email: newProfile.email || '',
          avatar: newProfile.avatar || '',
          phone: newProfile.phone || '',
          address: newProfile.address || '',
          age: newProfile.age || 18,
          bio: newProfile.bio || '',
          status: newProfile.status || 'active',
          roleIds: newProfile.roleIds || []
        })
      }
    }, { immediate: true, deep: true })
    
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
          
          // 更新本地数据
          localUserProfile.avatar = avatarUrl
          console.log('更新头像URL:', localUserProfile.avatar)
          ElMessage.success('头像上传成功')
          
          // 强制触发Vue响应式更新
          await nextTick()
          
          // 强制刷新头像显示
          const newAvatarUrl = getAvatarUrl(avatarUrl)
          console.log('新头像URL:', newAvatarUrl)
          console.log('原始头像路径:', avatarUrl)
          
          // 检查头像URL是否有效
          const testImage = new Image()
          testImage.onload = async () => {
            console.log('✅ 头像URL可访问:', newAvatarUrl)
            // 触发Vue组件强制重新渲染
            localUserProfile.avatar = ''
            await nextTick()
            localUserProfile.avatar = avatarUrl
            await nextTick()
          }
          testImage.onerror = () => {
            console.error('❌ 头像URL不可访问:', newAvatarUrl)
            console.error('请检查：')
            console.error('1. 后端服务器是否正常运行')
            console.error('2. 静态资源配置是否正确')
            console.error('3. 文件是否真实存在')
            ElMessage.error('头像文件无法访问，请检查服务器配置')
          }
          testImage.src = newAvatarUrl
          
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
    
    // 处理头像加载错误
    const handleAvatarError = () => {
      console.log('头像加载失败，使用默认头像')
      return true
    }
    
    // 保存用户头像
    const saveUserAvatar = async (avatarUrl) => {
      try {
        const profileData = {
          uid: localUserProfile.uid,
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
          // 通知Layout头像已更新
          localStorage.setItem('avatar_updated', Date.now().toString())
          // 触发storage事件
          window.dispatchEvent(new StorageEvent('storage', {
            key: 'avatar_updated',
            newValue: Date.now().toString()
          }))
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
          const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
          
          // 1. 保存用户基本信息
          const profileData = {
            uid: localUserProfile.uid,
            username: localUserProfile.username || '',
            gender: localUserProfile.gender || '保密',
            email: localUserProfile.email || '',
            avatar: localUserProfile.avatar || '',
            phone: localUserProfile.phone || '',
            address: localUserProfile.address !== undefined ? localUserProfile.address : '',
            age: localUserProfile.age || 18,
            bio: localUserProfile.bio !== undefined ? localUserProfile.bio : '',
            status: localUserProfile.status || 'active'
          }
          
          console.log('正在保存用户基本信息:', JSON.stringify(profileData))
          
          const profileRes = await axios.put(`/user/profile?uid=${profileData.uid}`, profileData, {
            headers: {
              Authorization: `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          })
          
          console.log('保存用户基本信息响应:', profileRes.data)
          
          if (profileRes.data.code !== 200) {
            ElMessage.error(profileRes.data.msg || '保存用户信息失败')
            return
          }
          
          // 2. 保存用户角色分配
          if (localUserProfile.roleIds && localUserProfile.roleIds.length >= 0) {
            console.log('正在分配用户角色:', localUserProfile.roleIds)
            
            try {
              const roleRes = await axios.post(`/role/assign?userId=${localUserProfile.uid}`, localUserProfile.roleIds, {
                headers: {
                  Authorization: `Bearer ${token}`,
                  'Content-Type': 'application/json'
                }
              })
              
              console.log('角色分配响应:', roleRes.data)
              
              if (roleRes.data.code !== 200) {
                console.warn('角色分配失败:', roleRes.data.msg)
                ElMessage.warning(`用户信息保存成功，但角色分配失败: ${roleRes.data.msg}`)
              } else {
                console.log('角色分配成功')
              }
            } catch (roleError) {
              console.error('角色分配API调用失败:', roleError)
              ElMessage.warning('用户信息保存成功，但角色分配失败')
            }
          }
          
          ElMessage.success('保存成功')
          dialogVisible.value = false
          
          // 只有当修改的是当前登录用户时，才更新本地存储的用户名
          const currentUserUid = parseInt(localStorage.getItem('uid') || sessionStorage.getItem('uid') || '0')
          if (profileData.uid === currentUserUid) {
            localStorage.setItem('username', profileData.username)
            sessionStorage.setItem('username', profileData.username)
          }
          
          emit('success')
          
        } catch (error) {
          console.error('保存用户信息失败', error)
          console.error('错误详情:', error.response ? error.response.data : '无响应数据')
          ElMessage.error('保存用户信息失败: ' + (error.message || '未知错误'))
        }
      })
    }
    
    return {
      dialogVisible,
      profileFormRef,
      profileRules,
      uploadHeaders,
      localUserProfile,
      getAvatarUrl,
      beforeAvatarUpload,
      handleAvatarSuccess,
      handleAvatarError,
      saveProfile
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px 0;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-preview {
  margin-bottom: 10px;
}

.avatar-view {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
</style> 