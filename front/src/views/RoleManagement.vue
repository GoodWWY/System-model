<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">角色管理</h2>
      <div class="header-actions">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddRoleDialog">添加角色</el-button>
        <el-button type="success" size="small" icon="el-icon-refresh" @click="initRoles">初始化角色</el-button>
      </div>
    </div>
    <div class="table-container">
      <el-table 
        :data="roleTableData" 
        stripe 
        border 
        class="custom-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="name" label="角色名称" width="120" align="center"></el-table-column>
        <el-table-column prop="code" label="角色代码" width="150" align="center"></el-table-column>
        <el-table-column prop="description" label="描述" align="center"></el-table-column>
        <el-table-column label="用户数量" width="100" align="center">
          <template #default="scope">
            <el-tag type="info" size="small">{{ scope.row.userCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="success" size="mini" @click="viewRoleUsers(scope.row)">查看用户</el-button>
              <el-button type="primary" size="mini" @click="handleEditRole(scope.row)">编辑</el-button>
              <el-button 
                type="danger" 
                size="mini" 
                @click="handleDeleteRole(scope.row)"
                :disabled="scope.row.code === 'ROLE_ADMIN'"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加角色对话框 -->
    <el-dialog v-model="addRoleDialogVisible" title="添加角色" width="500px">
      <el-form :model="addRoleForm" label-width="80px" :rules="addRoleRules" ref="addRoleFormRef">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="addRoleForm.name"></el-input>
        </el-form-item>
        <el-form-item label="角色代码" prop="code">
          <el-input v-model="addRoleForm.code"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="addRoleForm.description" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="addRoleForm.status" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align: right;">
          <el-button @click="addRoleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddRole">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑角色对话框 -->
    <el-dialog v-model="editRoleDialogVisible" title="编辑角色" width="500px" :destroy-on-close="true">
      <el-form :model="editRoleForm" label-width="80px" :rules="editRoleRules" ref="editRoleFormRef">
        <el-form-item label="ID" prop="id">
          <el-input v-model="editRoleForm.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="editRoleForm.name" :disabled="editRoleForm.code === 'ROLE_ADMIN'"></el-input>
        </el-form-item>
        <el-form-item label="角色代码" prop="code">
          <el-input v-model="editRoleForm.code" disabled></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="editRoleForm.description" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editRoleForm.status" active-text="启用" inactive-text="禁用" :disabled="editRoleForm.code === 'ROLE_ADMIN'"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align: right;">
          <el-button @click="editRoleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditRole">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 角色用户列表对话框 -->
    <el-dialog v-model="roleUsersDialogVisible" :title="`角色 '${currentRole.name}' 的用户列表`" width="800px">
      <div class="role-users-container">
        <div class="role-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="角色名称">{{ currentRole.name }}</el-descriptions-item>
            <el-descriptions-item label="角色代码">{{ currentRole.code }}</el-descriptions-item>
            <el-descriptions-item label="用户数量">{{ roleUsers.length }}</el-descriptions-item>
            <el-descriptions-item label="角色状态">
              <el-tag :type="currentRole.status === 'active' ? 'success' : 'danger'" size="small">
                {{ currentRole.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="users-table">
          <el-table 
            :data="roleUsers" 
            stripe 
            border 
            max-height="300"
            style="margin-top: 20px;"
          >
            <el-table-column prop="username" label="用户名" width="120" align="center"></el-table-column>
            <el-table-column prop="email" label="邮箱" align="center" show-overflow-tooltip></el-table-column>
            <el-table-column prop="phone" label="手机号" width="120" align="center"></el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 'active' ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="roleUsers.length === 0" class="no-users">
            <el-empty description="该角色暂无用户" :image-size="80"></el-empty>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div style="text-align: right;">
          <el-button @click="roleUsersDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'RoleManagement',
  setup() {
    const roleTableData = ref([])
    const roleTableTotal = ref(0)
    const addRoleDialogVisible = ref(false)
    const editRoleDialogVisible = ref(false)
    const roleUsersDialogVisible = ref(false)
    const addRoleFormRef = ref(null)
    const editRoleFormRef = ref(null)
    const roleUsers = ref([])
    const currentRole = ref({})
    
    const addRoleForm = reactive({
      name: '',
      code: '',
      description: '',
      status: true
    })
    
    const editRoleForm = reactive({
      id: null,
      name: '',
      code: '',
      description: '',
      status: true
    })
    
    const addRoleRules = {
      name: [
        { required: true, message: '请输入角色名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      code: [
        { required: true, message: '请输入角色代码', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
        { pattern: /^[A-Z_]+$/, message: '角色代码只能包含大写字母和下划线', trigger: 'blur' }
      ]
    }
    
    const editRoleRules = {
      name: [
        { required: true, message: '请输入角色名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ]
    }
    
    // 获取角色列表
    const fetchRoleList = async () => {
      console.log('开始获取角色列表...')
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        console.log('使用的token:', token)
        
        const res = await axios.get('/role/list', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        console.log('角色列表API响应:', res.data)
        
        if (res.data.code === 200) {
          let roles = res.data.data || []
          
          // 为每个角色获取用户数量
          for (let role of roles) {
            try {
              const userCountRes = await axios.get('/user/search', {
                headers: {
                  'Authorization': `Bearer ${token}`
                },
                params: {
                  roleId: role.id,
                  pageNum: 1,
                  pageSize: 1000 // 使用较大的pageSize来获取所有用户
                }
              })
              
              if (userCountRes.data.code === 200) {
                role.userCount = userCountRes.data.data.total || 0
              } else {
                role.userCount = 0
              }
            } catch (error) {
              console.error(`获取角色${role.name}的用户数量失败:`, error)
              role.userCount = 0
            }
          }
          
          roleTableData.value = roles
          roleTableTotal.value = roles.length
          console.log('获取到的角色列表（含用户数量）:', roleTableData.value)
        } else {
          ElMessage.error('获取角色列表失败')
          console.error('获取角色列表失败:', res.data.msg)
        }
      } catch (error) {
        console.error('获取角色列表失败:', error)
        if (error.response) {
          console.error('错误响应:', error.response.data)
          console.error('错误状态码:', error.response.status)
        }
        ElMessage.error('获取角色列表失败，使用模拟数据')
        // 模拟数据
        roleTableData.value = [
          { id: 1, name: '管理员', code: 'ROLE_ADMIN', description: '系统管理员，拥有所有权限', status: 'active', userCount: 2 },
          { id: 2, name: '普通用户', code: 'ROLE_USER', description: '普通用户，拥有基本权限', status: 'active', userCount: 4 },
          { id: 3, name: '访客', code: 'ROLE_GUEST', description: '访客用户，仅可查看', status: 'inactive', userCount: 0 }
        ]
        roleTableTotal.value = 3
      }
    }
    
    // 打开添加角色对话框
    const openAddRoleDialog = () => {
      addRoleForm.name = ''
      addRoleForm.code = ''
      addRoleForm.description = ''
      addRoleForm.status = true
      
      addRoleDialogVisible.value = true
      
      if (addRoleFormRef.value) {
        addRoleFormRef.value.resetFields()
      }
    }
    
    // 提交添加角色
    const submitAddRole = () => {
      if (!addRoleFormRef.value) return
      
      addRoleFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const roleData = {
              name: addRoleForm.name,
              code: addRoleForm.code,
              description: addRoleForm.description,
              status: addRoleForm.status ? 'active' : 'inactive'
            }
            
            const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
            const res = await axios.post('/role/add', roleData, {
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              }
            })
            
            if (res.data.code === 200) {
              ElMessage.success('添加角色成功')
              addRoleDialogVisible.value = false
              fetchRoleList()
            } else {
              ElMessage.error(res.data.msg || '添加角色失败')
            }
          } catch (error) {
            console.error('添加角色失败:', error)
            ElMessage.error('添加角色失败: ' + (error.message || '未知错误'))
          }
        }
      })
    }
    
    // 处理编辑角色
    const handleEditRole = (role) => {
      editRoleForm.id = role.id
      editRoleForm.name = role.name
      editRoleForm.code = role.code
      editRoleForm.description = role.description || ''
      editRoleForm.status = role.status === 'active'
      
      editRoleDialogVisible.value = true
      
      console.log('编辑角色:', role.id, role.name, role.code)
    }
    
    // 提交编辑角色
    const submitEditRole = () => {
      if (!editRoleFormRef.value) {
        console.error('编辑角色表单引用不存在')
        return
      }
      
      console.log('正在提交编辑角色表单:', JSON.stringify(editRoleForm))
      
      editRoleFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const roleData = {
              id: editRoleForm.id,
              name: editRoleForm.name,
              code: editRoleForm.code,
              description: editRoleForm.description,
              status: editRoleForm.status ? 'active' : 'inactive'
            }
            
            const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
            const res = await axios.put('/role/update', roleData, {
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              }
            })
            
            if (res.data.code === 200) {
              ElMessage.success('更新角色成功')
              editRoleDialogVisible.value = false
              fetchRoleList()
            } else {
              ElMessage.error(res.data.msg || '更新角色失败')
            }
          } catch (error) {
            console.error('更新角色失败:', error)
            ElMessage.error('更新角色失败: ' + (error.message || '未知错误'))
          }
        }
      })
    }
    
    // 处理删除角色
    const handleDeleteRole = (role) => {
      if (role.code === 'ROLE_ADMIN') {
        ElMessage.warning('管理员角色不能删除')
        return
      }
      
      ElMessageBox.confirm(`确认删除角色 ${role.name}?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
          const res = await axios.delete(`/role/delete/${role.id}`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('删除角色成功')
            fetchRoleList()
          } else {
            ElMessage.error(res.data.msg || '删除角色失败')
          }
        } catch (error) {
          console.error('删除角色失败:', error)
          ElMessage.error('删除角色失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 初始化角色
    const initRoles = async () => {
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const res = await axios.post('/role/init', {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        if (res.data.code === 200) {
          ElMessage.success('角色初始化成功')
          fetchRoleList()
        } else {
          ElMessage.error(res.data.msg || '角色初始化失败')
        }
      } catch (error) {
        console.error('角色初始化失败:', error)
        ElMessage.error('角色初始化失败: ' + (error.message || '未知错误'))
      }
    }
    
    // 查看角色用户
    const viewRoleUsers = async (role) => {
      currentRole.value = role
      
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        
        // 使用用户搜索接口，根据roleId获取用户列表
        const res = await axios.get('/user/search', {
          headers: {
            'Authorization': `Bearer ${token}`
          },
          params: {
            roleId: role.id,
            pageNum: 1,
            pageSize: 1000 // 获取所有用户
          }
        })
        
        if (res.data.code === 200) {
          const userData = res.data.data
          roleUsers.value = userData.records || []
          console.log('成功获取角色用户:', roleUsers.value.length, '个用户')
          console.log('用户列表:', roleUsers.value)
        } else {
          console.error('API返回错误:', res.data.msg)
          ElMessage.error(`获取角色 "${role.name}" 的用户列表失败: ${res.data.msg}`)
          roleUsers.value = []
        }
      } catch (error) {
        console.error('API调用失败:', error)
        if (error.response) {
          console.error('错误响应:', error.response.data)
          console.error('错误状态码:', error.response.status)
        }
        ElMessage.error(`获取角色 "${role.name}" 的用户列表失败`)
        roleUsers.value = []
      }
      
      roleUsersDialogVisible.value = true
    }
    
    onMounted(() => {
      fetchRoleList()
    })
    
    return {
      roleTableData,
      roleTableTotal,
      addRoleDialogVisible,
      editRoleDialogVisible,
      roleUsersDialogVisible,
      addRoleFormRef,
      editRoleFormRef,
      addRoleForm,
      editRoleForm,
      addRoleRules,
      editRoleRules,
      roleUsers,
      currentRole,
      fetchRoleList,
      openAddRoleDialog,
      submitAddRole,
      handleEditRole,
      submitEditRole,
      handleDeleteRole,
      initRoles,
      viewRoleUsers
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

.header-actions {
  display: flex;
  gap: 10px;
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.custom-table {
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.role-users-container {
  max-height: 500px;
  overflow-y: auto;
}

.role-info {
  margin-bottom: 20px;
}

.users-table {
  position: relative;
}

.no-users {
  padding: 20px;
  text-align: center;
}
</style> 