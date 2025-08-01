<template>
  <el-dialog v-model="dialogVisible" title="添加用户" width="500px">
    <el-form :model="addUserForm" label-width="80px" :rules="addUserRules" ref="addUserFormRef">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="addUserForm.username"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="addUserForm.password" type="password"></el-input>
      </el-form-item>
      <el-form-item label="头像">
        <div class="avatar-upload-container">
          <el-avatar 
            :size="80" 
            :src="addUserForm.avatar || ''" 
            @error="() => true"
          >
            <i class="el-icon-user-solid" style="font-size: 40px; color: #909399;"></i>
          </el-avatar>
          <div class="avatar-upload-info" v-if="addUserForm.avatar">
            <span class="avatar-filename">已上传头像</span>
          </div>
          <el-upload
            class="avatar-uploader"
            action="/user/upload-avatar"
            :headers="uploadHeaders"
            :data="{ uid: -1 }"
            :show-file-list="false"
            :on-success="handleAddUserAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-button type="primary" size="small" style="margin-top: 10px;">上传头像</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="addUserForm.gender">
          <el-radio label="男">男</el-radio>
          <el-radio label="女">女</el-radio>
          <el-radio label="保密">保密</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="addUserForm.email"></el-input>
      </el-form-item>
      <el-form-item label="手机号码" prop="phone">
        <el-input v-model="addUserForm.phone"></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="addUserForm.status" active-text="正常" inactive-text="禁用"></el-switch>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="addUserForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
          <el-option
            v-for="role in roleOptions"
            :key="role.id"
            :label="role.name"
            :value="role.id"
          ></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div style="text-align: right;">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddUser">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'AddUserDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    roleOptions: {
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
    
    const addUserFormRef = ref(null)
    
    const addUserForm = reactive({
      username: '',
      password: '',
      gender: '保密',
      email: '',
      phone: '',
      status: true,
      avatar: '',
      roleIds: []
    })
    
    const addUserRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
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
    
    // 处理添加用户头像上传成功
    const handleAddUserAvatarSuccess = (res) => {
      console.log('添加用户头像上传响应:', res)
      if (res.code === 200) {
        const avatarUrl = res.data
        console.log('上传成功，头像URL:', avatarUrl)
        
        addUserForm.avatar = avatarUrl
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error(res.msg || '头像上传失败')
      }
    }
    
    // 提交添加用户
    const submitAddUser = () => {
      addUserFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const userData = {
              username: addUserForm.username,
              password: addUserForm.password,
              gender: addUserForm.gender,
              email: addUserForm.email,
              phone: addUserForm.phone,
              avatar: addUserForm.avatar,
              status: addUserForm.status ? "active" : "inactive",
              roleIds: addUserForm.roleIds
            }
            
            console.log('提交用户数据:', userData)
            
            const res = await axios.post('/user/add', userData, {
              headers: {
                Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token') || ''}`
              }
            })
            
            if (res.data.code === 200) {
              ElMessage.success('用户添加成功')
              dialogVisible.value = false
              emit('success')
            } else {
              ElMessage.error(res.data.msg || '添加用户失败')
            }
          } catch (error) {
            console.error('添加用户失败:', error)
            ElMessage.error('添加用户失败: ' + (error.message || '未知错误'))
          }
        }
      })
    }
    
    // 重置表单
    const resetForm = () => {
      Object.assign(addUserForm, {
        username: '',
        password: '',
        gender: '保密',
        email: '',
        phone: '',
        status: true,
        avatar: '',
        roleIds: []
      })
      
      if (addUserFormRef.value) {
        addUserFormRef.value.resetFields()
      }
    }
    
    // 监听对话框显示状态，显示时重置表单
    watch(dialogVisible, (newVal) => {
      if (newVal) {
        resetForm()
      }
    })
    
    return {
      dialogVisible,
      addUserFormRef,
      addUserForm,
      addUserRules,
      uploadHeaders,
      beforeAvatarUpload,
      handleAddUserAvatarSuccess,
      submitAddUser
    }
  }
}
</script>

<style scoped>
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-upload-info {
  text-align: center;
}

.avatar-filename {
  font-size: 12px;
  color: #67C23A;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}
</style> 