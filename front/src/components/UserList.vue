<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">用户列表</h2>
      <div class="header-actions">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">添加用户</el-button>
        <el-button type="danger" size="small" icon="el-icon-delete" @click="handleBatchDelete" :disabled="!hasSelectedUsers">批量删除</el-button>
        <el-dropdown size="small" @command="handleColumnControl">
          <el-button type="default" size="small">
            列展示 <i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="(value, key) in columnOptions" :key="key">
                <el-checkbox v-model="columnVisible[key]">{{ value }}</el-checkbox>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 搜索面板 -->
    <div class="search-panel">
      <div class="search-form">
        <div class="search-form-title">
          <i class="el-icon-search"></i>
          <span>搜索条件</span>
        </div>
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="用户名">
            <el-input v-model="searchQuery" placeholder="请输入用户名" clearable class="search-input"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchStatus" placeholder="请选择状态" clearable>
              <el-option label="正常" value="1"></el-option>
              <el-option label="禁用" value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="searchGender" placeholder="请选择性别" clearable>
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
              <el-option label="保密" value="保密"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div class="search-buttons">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>
    
    <div class="table-container">
      <el-table 
        :data="userTableData" 
        stripe 
        border 
        class="custom-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="uid" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120" align="center" v-if="columnVisible.username"></el-table-column>
        <el-table-column label="头像" width="100" align="center" v-if="columnVisible.avatar">
          <template #default="scope">
            <el-avatar :size="40" :src="getAvatarUrl(scope.row.avatar)" @error="() => true">
              <img src="/picture/2.JPG" />
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center" v-if="columnVisible.gender"></el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" align="center" v-if="columnVisible.email"></el-table-column>
        <el-table-column prop="phone" label="电话" width="150" align="center" v-if="columnVisible.phone"></el-table-column>
        <el-table-column prop="roleName" label="角色" width="120" align="center" v-if="columnVisible.role"></el-table-column>
        <el-table-column label="状态" width="100" align="center" v-if="columnVisible.status">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-text="正常"
              inactive-text="禁用"
              @change="(val) => handleStatusChange(val, scope.row)"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="mini" @click="handleView(scope.row)">查看</el-button>
              <el-button type="warning" size="mini" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button 
                type="danger" 
                size="mini" 
                @click="handleDelete(scope.row)"
                :disabled="scope.row.username === 'admin'"
              >删除</el-button>
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
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'UserListView',
  setup() {
    // 用户表格相关
    const userTableData = ref([])
    const userTableTotal = ref(0)
    const userTablePageNum = ref(1)
    const userTablePageSize = ref(10)
    const searchQuery = ref('')
    const searchStatus = ref('')
    const searchGender = ref('')
    const hasSelectedUsers = ref(false)
    
    // 列显示控制
    const columnVisible = ref({
      username: true,
      avatar: true,
      gender: true,
      email: true,
      phone: true,
      role: true,
      status: true
    })
    
    const columnOptions = {
      username: '用户名',
      avatar: '头像',
      gender: '性别',
      email: '邮箱',
      phone: '电话',
      role: '角色',
      status: '状态'
    }
    
    // 搜索表单
    const searchForm = reactive({
      query: '',
      status: '',
      gender: ''
    })
    
    const fetchUsers = async (page = 1) => {
      userTablePageNum.value = page
      try {
        // 尝试从后端获取数据
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const res = await axios.get(`/user/list?pageNum=${page}&pageSize=${userTablePageSize.value}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        const data = res.data
        
        if(data.code === 200 && data.data && data.data.records) {
          // 处理后端返回的数据
          console.log('后端返回的原始数据:', data.data.records)
          
          userTableData.value = data.data.records.map(user => {
            return {
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
            }
          })
          
          userTableTotal.value = data.data.total
          console.log('处理后的用户列表数据:', userTableData.value)
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
    
    const handleSizeChange = (size) => {
      userTablePageSize.value = size
      fetchUsers(1)
    }
    
    // 处理选择变化
    const handleSelectionChange = (val) => {
      hasSelectedUsers.value = val.length > 0
    }
    
    // 处理搜索
    const handleSearch = () => {
      const conditions = {
        query: searchQuery.value,
        status: searchStatus.value,
        gender: searchGender.value
      }
      
      fetchFilteredUsers(conditions)
    }
    
    // 处理重置
    const handleReset = () => {
      searchQuery.value = ''
      searchStatus.value = ''
      searchGender.value = ''
      fetchUsers(1)
    }
    
    // 获取筛选后的用户列表
    const fetchFilteredUsers = async (conditions) => {
      try {
        // 构建查询参数
        const params = new URLSearchParams()
        
        if (conditions.query) {
          params.append('keyword', conditions.query)
        }
        
        if (conditions.status) {
          params.append('status', conditions.status)
        }
        
        if (conditions.gender) {
          params.append('gender', conditions.gender)
        }
        
        // 发送请求到后端搜索API
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
        const res = await axios.get(`/user/search?${params.toString()}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        const data = res.data
        
        if (data.code === 200 && data.data && data.data.records) {
          // 处理后端返回的数据
          userTableData.value = data.data.records.map(user => {
            return {
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
            }
          })
          userTableTotal.value = data.data.total
          
          ElMessage.success(`搜索完成，共找到 ${data.data.total} 条记录`)
        } else {
          throw new Error('获取数据格式不正确')
        }
      } catch (error) {
        console.error('搜索用户失败:', error)
        ElMessage.error('搜索用户失败，请检查网络连接')
        
        // 模拟搜索
        simulateSearch(conditions)
      }
    }
    
    // 模拟搜索结果（仅开发环境使用）
    const simulateSearch = (conditions) => {
      const filteredData = [
        { id: 1, username: 'user1', password: '******', status: false, selected: false, avatar: '/picture/2.JPG', gender: '男', email: 'user1@example.com', phone: '13800112233', uid: 1, roleName: '普通用户' },
        { id: 2, username: 'user2', password: '******', status: true, selected: false, avatar: '/picture/2.JPG', gender: '女', email: 'user2@example.com', phone: '13800112244', uid: 2, roleName: '管理员' },
        { id: 3, username: 'user3', password: '******', status: true, selected: false, avatar: '/picture/2.JPG', gender: '保密', email: 'user3@example.com', phone: '13800112255', uid: 3, roleName: '普通用户' },
        { id: 4, username: 'user4', password: '******', status: false, selected: false, avatar: '/picture/2.JPG', gender: '男', email: 'user4@example.com', phone: '13800112266', uid: 4, roleName: '普通用户' },
        { id: 5, username: 'user5', password: '******', status: true, selected: false, avatar: '/picture/2.JPG', gender: '女', email: 'user5@example.com', phone: '13800112277', uid: 5, roleName: '普通用户' }
      ].filter(user => {
        // 根据搜索条件过滤
        let match = true;
        
        if (conditions.query) {
          const keyword = conditions.query.toLowerCase();
          match = match && (
            user.username.toLowerCase().includes(keyword) || 
            (user.email && user.email.toLowerCase().includes(keyword)) || 
            (user.phone && user.phone.includes(keyword))
          );
        }
        
        if (conditions.status) {
          const statusValue = conditions.status === '1';
          match = match && user.status === statusValue;
        }
        
        if (conditions.gender) {
          match = match && user.gender === conditions.gender;
        }
        
        return match;
      });
      
      userTableData.value = filteredData;
      userTableTotal.value = filteredData.length;
      
      ElMessage.success(`搜索完成，共找到 ${filteredData.length} 条记录`);
    };
    
    // 列控制
    const handleColumnControl = () => {
      // 这里不需要做什么，因为el-checkbox会自动改变columnVisible的值
      console.log('当前列显示状态:', columnVisible.value);
    }
    
    // 状态变化
    const handleStatusChange = async (val, row) => {
      try {
        // 这里应该调用后端API更新用户状态
        const status = val ? 'active' : 'inactive';
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
        
        const res = await axios.put(`/user/status?uid=${row.uid}&status=${status}`, {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (res.data.code === 200) {
          ElMessage.success(`用户 ${row.username} 状态已${val ? '启用' : '禁用'}`);
        } else {
          throw new Error(res.data.msg || '更新用户状态失败');
        }
      } catch (error) {
        console.error('更新用户状态失败:', error);
        // 更新失败，恢复原状态
        row.status = !val;
        ElMessage.error('更新用户状态失败，请重试');
      }
    }
    
    // 查看用户
    const handleView = (row) => {
      ElMessage.info(`查看用户: ${row.username}`);
    }
    
    // 编辑用户
    const handleEdit = (row) => {
      ElMessage.info(`编辑用户: ${row.username}`);
    }
    
    // 删除用户
    const handleDelete = (row) => {
      if (row.username === 'admin') {
        ElMessage.warning('系统管理员不能删除');
        return;
      }
      
      ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
          const res = await axios.delete(`/user/delete?uid=${row.uid}`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          if (res.data.code === 200) {
            ElMessage.success('删除用户成功');
            // 刷新用户列表
            fetchUsers(userTablePageNum.value);
          } else {
            throw new Error(res.data.msg || '删除用户失败');
          }
        } catch (error) {
          console.error('删除用户失败:', error);
          ElMessage.error('删除用户失败，请重试');
        }
      }).catch(() => {
        // 用户取消删除
      });
    }
    
    // 批量删除
    const handleBatchDelete = () => {
      const selectedRows = userTableData.value.filter(row => row.selected);
      if (selectedRows.length === 0) {
        ElMessage.warning('请先选择要删除的用户');
        return;
      }
      
      const adminSelected = selectedRows.some(row => row.username === 'admin');
      if (adminSelected) {
        ElMessage.warning('不能删除管理员用户');
        return;
      }
      
      ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.length} 个用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 这里应该调用后端API批量删除用户
          const uids = selectedRows.map(row => row.uid);
          const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
          
          const res = await axios.post('/user/batch-delete', uids, {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          });
          
          if (res.data.code === 200) {
            ElMessage.success(`成功删除 ${selectedRows.length} 个用户`);
            // 刷新用户列表
            fetchUsers(userTablePageNum.value);
          } else {
            throw new Error(res.data.msg || '批量删除用户失败');
          }
        } catch (error) {
          console.error('批量删除用户失败:', error);
          ElMessage.error('批量删除用户失败，请重试');
        }
      }).catch(() => {
        // 用户取消删除
      });
    }
    
    // 添加用户
    const handleAdd = () => {
      ElMessage.info('添加用户功能即将上线');
    }
    
    // 获取头像URL
    const getAvatarUrl = (url) => {
      if (!url) {
        return '/picture/2.JPG';
      }
      // 如果是OSS的完整URL，直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      // 兼容旧的相对路径格式
      return url.startsWith('/') ? url : `/${url}`;
    }
    
    onMounted(() => {
      fetchUsers(1)
    })
    
    return {
      userTableData,
      userTableTotal,
      userTablePageNum,
      userTablePageSize,
      searchQuery,
      searchStatus,
      searchGender,
      columnVisible,
      columnOptions,
      searchForm,
      hasSelectedUsers,
      fetchUsers,
      handleSizeChange,
      handleSelectionChange,
      handleSearch,
      handleReset,
      handleColumnControl,
      handleStatusChange,
      handleView,
      handleEdit,
      handleDelete,
      handleBatchDelete,
      handleAdd,
      getAvatarUrl
    }
  }
}
</script>

<style scoped>
.content-section {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.table-container {
  width: 100%;
  overflow-x: hidden;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 搜索面板样式 */
.search-panel {
  margin-bottom: 20px;
}

.search-form {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);
}

.search-form-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.search-form-title i {
  margin-right: 8px;
  color: #409EFF;
  font-size: 18px;
}

.search-input {
  width: 100%;
}

.search-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 15px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-buttons .el-button {
  padding: 6px 12px;
  margin: 0;
  font-size: 12px;
}

/* 表格样式调整 */
:deep(.el-table__header th) {
  text-align: center;
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table__row) {
  height: 50px;
}

:deep(.el-table__cell) {
  padding: 8px 0;
}

:deep(.el-table__body td) {
  text-align: center;
}
</style> 