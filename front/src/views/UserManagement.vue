<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">用户列表</h2>
      <div class="header-actions">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddUserDialog">添加用户</el-button>
      </div>
    </div>
    <div class="table-container">
      <div class="search-panel">
        <div class="search-form">
          <div class="search-form-title">
            <i class="el-icon-search"></i>
            <span>搜索条件</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-input
                placeholder="搜索用户名/邮箱/手机号"
                prefix-icon="el-icon-search"
                v-model="searchQuery"
                clearable
                class="search-input"
              ></el-input>
            </el-col>
            <el-col :span="4">
              <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 100%">
                <el-option label="全部" value=""></el-option>
                <el-option label="正常" value="active"></el-option>
                <el-option label="禁用" value="inactive"></el-option>
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="searchGender" placeholder="性别" clearable style="width: 100%">
                <el-option label="全部" value=""></el-option>
                <el-option label="男" value="男"></el-option>
                <el-option label="女" value="女"></el-option>
                <el-option label="保密" value="保密"></el-option>
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="searchRoleId" placeholder="角色" clearable style="width: 100%">
                <el-option label="全部" value=""></el-option>
                <el-option v-for="role in roleOptions" :key="role.id" :label="role.name" :value="role.id"></el-option>
              </el-select>
            </el-col>
            <el-col :span="6">
              <div class="search-buttons">
                <el-button type="primary" icon="el-icon-search" @click="handleSearch" size="default">搜索</el-button>
                <el-button icon="el-icon-refresh" @click="handleReset" size="default">重置</el-button>
              </div>
            </el-col>
          </el-row>
        </div>
        <div class="table-toolbar">
          <div class="left-actions">
            <el-button type="danger" icon="el-icon-delete" size="small" :disabled="!hasSelected" @click="handleBatchDelete">批量删除</el-button>
          </div>
          <div class="right-actions">
            <el-button type="info" icon="el-icon-download" size="small" @click="handleExport" :loading="exporting">导出</el-button>
            <el-button type="primary" icon="el-icon-refresh" size="small" @click="fetchUsers(userTablePageNum)">刷新</el-button>
            <el-dropdown trigger="click">
              <el-button type="default" size="small">
                列设置 <i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-checkbox v-model="columnVisible.username">用户名</el-checkbox>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-checkbox v-model="columnVisible.avatar">头像</el-checkbox>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-checkbox v-model="columnVisible.gender">性别</el-checkbox>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-checkbox v-model="columnVisible.email">邮箱</el-checkbox>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-checkbox v-model="columnVisible.phone">手机号</el-checkbox>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-checkbox v-model="columnVisible.status">状态</el-checkbox>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-checkbox v-model="columnVisible.role">角色</el-checkbox>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      <el-table 
        :data="userTableData" 
        stripe 
        border 
        class="custom-table"
      >
        <el-table-column width="55" align="center">
          <template #header>
            <el-checkbox :model-value="allSelected" @change="handleSelectAll"></el-checkbox>
          </template>
          <template #default="scope">
            <el-checkbox :model-value="scope.row.selected" @change="(val) => handleSelectionChange(val, scope.row)"></el-checkbox>
          </template>
        </el-table-column>
        
        <el-table-column v-if="columnVisible.username" label="用户名" width="120" align="center">
          <template #default="scope">
            <div class="username-cell">
              <span class="username-text">{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column v-if="columnVisible.avatar" label="头像" width="80" align="center">
          <template #default="scope">
            <el-avatar 
              :size="40" 
              :src="scope.row.avatar" 
              @error="() => true"
            >
              <i class="el-icon-user-solid" style="font-size: 20px; color: #909399;"></i>
            </el-avatar>
          </template>
        </el-table-column>
        
        <el-table-column v-if="columnVisible.gender" label="性别" width="80" align="center">
          <template #default="scope">
            <span>{{ scope.row.gender || '保密' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="columnVisible.email" label="邮箱" width="180" align="center" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.email || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="columnVisible.phone" label="手机号" width="120" align="center" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.phone || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="columnVisible.status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column v-if="columnVisible.role" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.roleName && scope.row.roleName.includes('管理员')" type="danger">
              {{ scope.row.roleName }}
            </el-tag>
            <el-tag v-else-if="scope.row.roleName" type="info">
              {{ scope.row.roleName }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="success" size="mini" @click="viewUserProfile(scope.row)">查看</el-button>
              <el-button type="primary" size="mini" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="userTableTotal"
          :page-size="userTablePageSize"
          :current-page="userTablePageNum"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="fetchUsers"
          @size-change="handleSizeChange"
        ></el-pagination>
      </div>
    </div>

    <!-- 添加用户对话框 -->
    <AddUserDialog 
      v-model:visible="addUserDialogVisible"
      :role-options="roleOptions"
      @success="fetchUsers"
    />

    <!-- 用户详情对话框 -->
    <UserProfileDialog 
      v-model:visible="viewProfileDialogVisible"
      :user-profile="userProfile"
      :editable="editDialogVisible"
      :role-options="roleOptions"
      :user-roles="currentUserRoles"
      @success="handleProfileSuccess"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import AddUserDialog from '../components/AddUserDialog.vue'
import UserProfileDialog from '../components/UserProfileDialog.vue'

export default {
  name: 'UserManagement',
  components: {
    AddUserDialog,
    UserProfileDialog
  },
  setup() {
    const searchQuery = ref('')
    const searchStatus = ref('')
    const searchGender = ref('')
    const searchRoleId = ref('')
    const userTableData = ref([])
    const userTableTotal = ref(0)
    const userTablePageNum = ref(1)
    const userTablePageSize = ref(10)
    const addUserDialogVisible = ref(false)
    const viewProfileDialogVisible = ref(false)
    const editDialogVisible = ref(false)
    const roleOptions = ref([])
    const currentUserRoles = ref([])
    const exporting = ref(false)
    
    const userProfile = reactive({
      uid: 0,
      username: '',
      gender: '保密',
      email: '',
      avatar: '',
      phone: '',
      address: '',
      age: 18,
      bio: '',
      roleIds: []
    })
    
    // 列显示控制
    const columnVisible = ref({
      username: true,
      avatar: true,
      gender: true,
      email: true,
      phone: true,
      status: true,
      role: true
    })
    
    // 处理选择变化
    const handleSelectionChange = (val, row) => {
      const index = userTableData.value.findIndex(item => item.uid === row.uid)
      if (index !== -1) {
        userTableData.value[index].selected = val
      }
    }
    
    // 处理全选
    const allSelected = computed(() => {
      return userTableData.value.length > 0 && userTableData.value.every(item => item.selected)
    })
    
    const handleSelectAll = (val) => {
      userTableData.value.forEach(item => {
        item.selected = val
      })
    }
    
    // 判断是否有选中的项目
    const hasSelected = computed(() => {
      return userTableData.value.some(item => item.selected)
    })
    
    // 获取用户列表
    const fetchUsers = async (page = 1) => {
      userTablePageNum.value = page
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const res = await axios.get(`/user/list?pageNum=${page}&pageSize=${userTablePageSize.value}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        const data = res.data
        
        if(data.code === 200 && data.data && data.data.records) {
          userTableData.value = data.data.records.map(user => ({
            id: user.uid,
            uid: user.uid,
            username: user.username,
            password: '******',
            status: user.status === 'active',
            selected: false,
            avatar: user.avatar || '',
            gender: user.gender || '保密',
            email: user.email || '',
            phone: user.phone || '',
            address: user.address || '',
            bio: user.bio || '',
            age: user.age || 18,
            roleName: user.roleName || ''
          }))
          
          userTableTotal.value = data.data.total
        } else {
          throw new Error('获取数据格式不正确')
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败，请检查网络连接')
        // 模拟数据
        userTableData.value = [
          { id: 1, username: 'user1', password: '******', status: false, selected: false, avatar: '/picture/2.JPG', gender: '男', email: 'user1@example.com', phone: '13800112233', uid: 1, roleName: '普通用户' },
          { id: 2, username: 'user2', password: '******', status: true, selected: false, avatar: '/picture/2.JPG', gender: '女', email: 'user2@example.com', phone: '13800112244', uid: 2, roleName: '管理员' },
          { id: 3, username: 'user3', password: '******', status: true, selected: false, avatar: '/picture/2.JPG', gender: '保密', email: 'user3@example.com', phone: '13800112255', uid: 3, roleName: '普通用户' },
          { id: 4, username: 'user4', password: '******', status: false, selected: false, avatar: '/picture/2.JPG', gender: '男', email: 'user4@example.com', phone: '13800112266', uid: 4, roleName: '普通用户' },
          { id: 5, username: 'user5', password: '******', status: true, selected: false, avatar: '/picture/2.JPG', gender: '女', email: 'user5@example.com', phone: '13800112277', uid: 5, roleName: '普通用户' }
        ]
        userTableTotal.value = 100
      }
    }
    
    // 处理页面大小变化
    const handleSizeChange = (size) => {
      userTablePageSize.value = size
      fetchUsers(1)
    }
    
    // 处理搜索
    const handleSearch = () => {
      const conditions = {
        query: searchQuery.value,
        status: searchStatus.value,
        gender: searchGender.value,
        roleId: searchRoleId.value
      }
      
      console.log('搜索条件:', conditions)
      fetchFilteredUsers(conditions)
    }
    
    // 重置搜索条件
    const handleReset = () => {
      searchQuery.value = ''
      searchStatus.value = ''
      searchGender.value = ''
      searchRoleId.value = ''
      fetchUsers(1)
    }
    
    // 根据条件获取过滤后的用户数据
    const fetchFilteredUsers = async (conditions) => {
      try {
        const params = new URLSearchParams()
        params.append('pageNum', userTablePageNum.value)
        params.append('pageSize', userTablePageSize.value)
        
        if (conditions.query) {
          params.append('keyword', conditions.query)
        }
        if (conditions.status) {
          params.append('status', conditions.status)
        }
        if (conditions.gender) {
          params.append('gender', conditions.gender)
        }
        if (conditions.roleId) {
          params.append('roleId', conditions.roleId)
        }
        
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const res = await axios.get(`/user/search?${params.toString()}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        const data = res.data
        
        if (data.code === 200 && data.data && data.data.records) {
          userTableData.value = data.data.records.map(user => ({
            id: user.uid,
            uid: user.uid,
            username: user.username,
            password: '******',
            status: user.status === 'active',
            selected: false,
            avatar: user.avatar || '',
            gender: user.gender || '保密',
            email: user.email || '',
            phone: user.phone || '',
            address: user.address || '',
            bio: user.bio || '',
            age: user.age || 18,
            roleName: user.roleName || ''
          }))
          userTableTotal.value = data.data.total
          
          ElMessage.success(`搜索完成，共找到 ${data.data.total} 条记录`)
        } else {
          throw new Error('获取数据格式不正确')
        }
      } catch (error) {
        console.error('搜索用户失败:', error)
        ElMessage.error('搜索用户失败，请检查网络连接')
      }
    }
    
    // 批量删除
    const handleBatchDelete = () => {
      const selectedItems = userTableData.value.filter(item => item.selected)
      if (selectedItems.length === 0) {
        ElMessage.warning('请先选择要删除的项目')
        return
      }
      
      ElMessageBox.confirm(`确认删除选中的 ${selectedItems.length} 项?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const uids = selectedItems.map(item => item.uid)
          
          const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
          const res = await axios.delete('/user/batch', {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            },
            data: uids
          })
          
          if (res.data.code === 200) {
            userTableData.value = userTableData.value.filter(item => !item.selected)
            ElMessage.success('批量删除成功')
          } else {
            ElMessage.error(res.data.msg || '批量删除失败')
          }
        } catch (error) {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 获取用户角色信息
    const fetchUserRoles = async (uid) => {
      // 根据用户ID生成模拟角色数据
      const generateMockUserRoles = (userId) => {
        const userRoleMap = {
          1: [{ id: 1, name: '管理员', code: 'ROLE_ADMIN' }], // admin用户
          2: [{ id: 1, name: '管理员', code: 'ROLE_ADMIN' }], // test1234用户  
          3: [{ id: 2, name: '普通用户', code: 'ROLE_USER' }], // wwy123用户
          4: [{ id: 2, name: '普通用户', code: 'ROLE_USER' }], // admin123用户
          5: [{ id: 2, name: '普通用户', code: 'ROLE_USER' }]  // test用户
        }
        return userRoleMap[userId] || [{ id: 2, name: '普通用户', code: 'ROLE_USER' }]
      }
      
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const res = await axios.get(`/user/roles?uid=${uid}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        if (res.data.code === 200) {
          currentUserRoles.value = res.data.data || []
          userProfile.roleIds = currentUserRoles.value.map(role => role.id)
          console.log('成功获取用户角色:', currentUserRoles.value)
        } else {
          // API返回错误，使用模拟数据
          currentUserRoles.value = generateMockUserRoles(uid)
          userProfile.roleIds = currentUserRoles.value.map(role => role.id)
          console.log('API返回错误，使用模拟用户角色数据:', currentUserRoles.value)
        }
      } catch (error) {
        // API调用失败，使用模拟数据
        currentUserRoles.value = generateMockUserRoles(uid)
        userProfile.roleIds = currentUserRoles.value.map(role => role.id)
        console.log('API调用失败，使用模拟用户角色数据:', currentUserRoles.value)
      }
    }
    
    // 处理编辑用户
    const handleEdit = async (row) => {
      Object.assign(userProfile, {
        uid: row.uid,
        username: row.username,
        gender: row.gender || '保密',
        email: row.email || '',
        avatar: row.avatar || '',
        phone: row.phone || '',
        address: row.address || '',
        age: row.age || 18,
        bio: row.bio || '',
        roleIds: []
      })
      
      // 获取用户角色信息
      await fetchUserRoles(row.uid)
      
      editDialogVisible.value = true
      viewProfileDialogVisible.value = true
    }
    
    // 处理删除用户
    const handleDelete = (row) => {
      ElMessageBox.confirm(`确认删除用户 ${row.username}?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
          const res = await axios.delete('/user/batch', {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            },
            data: [row.uid]
          })
          
          if (res.data.code === 200) {
            const index = userTableData.value.findIndex(item => item.uid === row.uid)
            if (index !== -1) {
              userTableData.value.splice(index, 1)
            }
            ElMessage.success('删除用户成功')
          } else {
            ElMessage.error(res.data.msg || '删除用户失败')
          }
        } catch (error) {
          console.error('删除用户失败:', error)
          ElMessage.error('删除用户失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 查看用户详情
    const viewUserProfile = async (row) => {
      Object.assign(userProfile, {
        uid: row.uid,
        username: row.username,
        gender: row.gender || '保密',
        email: row.email || '',
        avatar: row.avatar || '',
        phone: row.phone || '',
        address: row.address || '',
        age: row.age || 18,
        bio: row.bio || '',
        roleIds: []
      })
      
      // 获取用户角色信息
      await fetchUserRoles(row.uid)
      
      editDialogVisible.value = false
      viewProfileDialogVisible.value = true
    }
    
    // 打开添加用户对话框
    const openAddUserDialog = () => {
      addUserDialogVisible.value = true
    }
    
    // 处理资料保存成功
    const handleProfileSuccess = () => {
      fetchUsers(userTablePageNum.value)
    }
    
    // 获取所有角色
    const fetchRoles = async () => {
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const res = await axios.get('/role/list', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        if (res.data.code === 200) {
          roleOptions.value = res.data.data
          console.log('成功获取角色列表:', roleOptions.value)
        } else {
          // API返回错误，使用模拟数据
          roleOptions.value = [
            { id: 1, name: '管理员', code: 'ROLE_ADMIN', description: '系统管理员，拥有所有权限', status: 'active' },
            { id: 2, name: '普通用户', code: 'ROLE_USER', description: '普通用户，拥有基本权限', status: 'active' },
            { id: 3, name: '访客', code: 'ROLE_GUEST', description: '访客用户，仅可查看', status: 'inactive' }
          ]
          console.log('API返回错误，使用模拟角色数据:', roleOptions.value)
        }
      } catch (error) {
        // API调用失败，使用模拟数据
        roleOptions.value = [
          { id: 1, name: '管理员', code: 'ROLE_ADMIN', description: '系统管理员，拥有所有权限', status: 'active' },
          { id: 2, name: '普通用户', code: 'ROLE_USER', description: '普通用户，拥有基本权限', status: 'active' },
          { id: 3, name: '访客', code: 'ROLE_GUEST', description: '访客用户，仅可查看', status: 'inactive' }
        ]
        console.log('API调用失败，使用模拟角色数据:', roleOptions.value)
      }
    }
    
    // 处理导出功能
    const handleExport = async () => {
      exporting.value = true
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        
        // 构建查询参数
        const params = new URLSearchParams()
        if (searchQuery.value) {
          params.append('keyword', searchQuery.value)
        }
        if (searchStatus.value) {
          params.append('status', searchStatus.value)
        }
        if (searchGender.value) {
          params.append('gender', searchGender.value)
        }
        if (searchRoleId.value) {
          params.append('roleId', searchRoleId.value)
        }
        
        // 构建URL
        const url = `/user/export?${params.toString()}`
        
        console.log('开始导出用户数据，URL:', url)
        
        // 使用fetch API下载文件
        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        if (!response.ok) {
          // 检查是否是JSON错误响应
          const contentType = response.headers.get('Content-Type')
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json()
            throw new Error(errorData.msg || `HTTP error! status: ${response.status}`)
          } else {
            throw new Error(`HTTP error! status: ${response.status}`)
          }
        }
        
        // 检查响应类型
        const contentType = response.headers.get('Content-Type')
        if (contentType && contentType.includes('application/json')) {
          // 如果返回的是JSON，说明出错了
          const errorData = await response.json()
          throw new Error(errorData.msg || '导出失败')
        }
        
        // 获取文件名（从响应头中获取）
        const contentDisposition = response.headers.get('Content-Disposition')
        let filename = `用户列表_${new Date().getTime()}.xlsx`
        if (contentDisposition) {
          const matches = contentDisposition.match(/filename[^;=\n]*=([^;\n]*)/)
          if (matches && matches[1]) {
            filename = matches[1].replace(/['"]/g, '')
            // 处理中文文件名的编码问题
            try {
              filename = decodeURIComponent(filename)
            } catch (e) {
              console.warn('文件名解码失败，使用原始文件名:', filename)
            }
          }
        }
        
        // 获取文件数据
        const blob = await response.blob()
        
        // 检查文件大小
        if (blob.size === 0) {
          throw new Error('导出的文件为空')
        }
        
        // 创建下载链接
        const downloadUrl = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = filename
        
        // 触发下载
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        // 清理URL对象
        window.URL.revokeObjectURL(downloadUrl)
        
        console.log('用户数据导出成功，文件名:', filename, '文件大小:', blob.size, 'bytes')
        ElMessage.success(`用户数据导出成功！文件：${filename}`)
        
      } catch (error) {
        console.error('导出用户数据失败:', error)
        ElMessage.error('导出失败: ' + (error.message || '未知错误'))
      } finally {
        exporting.value = false
      }
    }
    
    onMounted(() => {
      fetchUsers()
      fetchRoles()
    })
    
    return {
      searchQuery,
      searchStatus,
      searchGender,
      searchRoleId,
      userTableData,
      userTableTotal,
      userTablePageNum,
      userTablePageSize,
      addUserDialogVisible,
      viewProfileDialogVisible,
      editDialogVisible,
      roleOptions,
      currentUserRoles,
      userProfile,
      columnVisible,
      allSelected,
      hasSelected,
      handleSelectionChange,
      handleSelectAll,
      fetchUsers,
      handleSizeChange,
      handleSearch,
      handleReset,
      handleBatchDelete,
      handleEdit,
      handleDelete,
      viewUserProfile,
      openAddUserDialog,
      handleProfileSuccess,
      fetchUserRoles,
      exporting,
      handleExport
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

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.search-panel {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.search-form-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-weight: 600;
  color: #606266;
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-start;
}

.search-buttons .el-button {
  padding: 8px 20px;
  font-weight: 500;
  border-radius: 6px;
}

.search-buttons .el-button--primary {
  background: #409eff;
  border-color: #409eff;
}

.search-buttons .el-button--primary:hover {
  background: #66b1ff;
  border-color: #66b1ff;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.left-actions,
.right-actions {
  display: flex;
  gap: 10px;
}

.custom-table {
  margin: 0;
}

.username-cell {
  display: flex;
  align-items: center;
}

.username-text {
  font-weight: 500;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #ebeef5;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .search-form .el-row {
    flex-wrap: wrap;
  }
  
  .search-form .el-col {
    margin-bottom: 12px;
  }
  
  .search-buttons {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .search-form .el-col {
    flex: 1 1 100%;
    max-width: 100%;
  }
  
  .search-buttons {
    justify-content: center;
    width: 100%;
  }
  
  .search-buttons .el-button {
    flex: 1;
    max-width: 120px;
  }
}
</style> 