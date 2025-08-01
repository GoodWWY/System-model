<template>
  <div class="permission-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">权限设置</h1>
      <p class="page-desc">管理系统权限，分配角色权限</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ statistics.total || 0 }}</div>
              <div class="stat-label">总权限</div>
            </div>
            <el-icon class="stat-icon"><Lock /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card enabled">
            <div class="stat-content">
              <div class="stat-value">{{ statistics.enabled || 0 }}</div>
              <div class="stat-label">启用权限</div>
            </div>
            <el-icon class="stat-icon"><Check /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card menu">
            <div class="stat-content">
              <div class="stat-value">{{ statistics.menu || 0 }}</div>
              <div class="stat-label">菜单权限</div>
            </div>
            <el-icon class="stat-icon"><Menu /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card api">
            <div class="stat-content">
              <div class="stat-value">{{ statistics.api || 0 }}</div>
              <div class="stat-label">API权限</div>
            </div>
            <el-icon class="stat-icon"><Connection /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card button">
            <div class="stat-content">
              <div class="stat-value">{{ statistics.button || 0 }}</div>
              <div class="stat-label">按钮权限</div>
            </div>
            <el-icon class="stat-icon"><Switch /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card disabled">
            <div class="stat-content">
              <div class="stat-value">{{ statistics.disabled || 0 }}</div>
              <div class="stat-label">禁用权限</div>
            </div>
            <el-icon class="stat-icon"><CircleClose /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 权限管理主体 -->
    <el-card class="main-card">
      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="search-area">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索权限名称、编码或描述"
            style="width: 300px; margin-right: 10px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select
            v-model="searchForm.category"
            placeholder="权限分类"
            style="width: 120px; margin-right: 10px"
            clearable
          >
            <el-option label="菜单权限" value="MENU" />
            <el-option label="API权限" value="API" />
            <el-option label="按钮权限" value="BUTTON" />
          </el-select>
          <el-select
            v-model="searchForm.isEnabled"
            placeholder="状态"
            style="width: 100px; margin-right: 10px"
            clearable
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        <div class="action-area">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新建权限
          </el-button>
          <el-button 
            type="success" 
            :disabled="selectedPermissions.length === 0"
            @click="handleBatchEnable"
          >
            <el-icon><Check /></el-icon>
            批量启用
          </el-button>
          <el-button 
            type="warning" 
            :disabled="selectedPermissions.length === 0"
            @click="handleBatchDisable"
          >
            <el-icon><Close /></el-icon>
            批量禁用
          </el-button>
          <el-button 
            type="danger" 
            :disabled="selectedPermissions.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>

      <!-- 权限列表 -->
      <el-table
        v-loading="loading"
        :data="permissionList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="权限名称" min-width="200">
          <template #default="{ row }">
            <span class="permission-name">{{ row.name }}</span>
            <el-tag 
              v-if="row.category === 'MENU'" 
              type="success" 
              size="small" 
              style="margin-left: 8px"
            >
              菜单
            </el-tag>
            <el-tag 
              v-else-if="row.category === 'API'" 
              type="primary" 
              size="small" 
              style="margin-left: 8px"
            >
              API
            </el-tag>
            <el-tag 
              v-else-if="row.category === 'BUTTON'" 
              type="warning" 
              size="small" 
              style="margin-left: 8px"
            >
              按钮
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="权限编码" width="200">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="权限描述" min-width="250" />
        <el-table-column prop="resource" label="资源路径" width="200" />
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag 
              v-if="row.action === 'ALL'"
              type="danger"
              size="small"
            >
              ALL
            </el-tag>
            <el-tag 
              v-else-if="row.action === 'CREATE'"
              type="success"
              size="small"
            >
              CREATE
            </el-tag>
            <el-tag 
              v-else-if="row.action === 'READ'"
              type="primary"
              size="small"
            >
              READ
            </el-tag>
            <el-tag 
              v-else-if="row.action === 'UPDATE'"
              type="warning"
              size="small"
            >
              UPDATE
            </el-tag>
            <el-tag 
              v-else-if="row.action === 'DELETE'"
              type="danger"
              size="small"
            >
              DELETE
            </el-tag>
            <span v-else>{{ row.action }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="isEnabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.isEnabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 权限表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="permissionFormRef"
        :model="permissionForm"
        :rules="permissionRules"
        label-width="100px"
      >
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="permissionForm.code" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限描述">
          <el-input
            v-model="permissionForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入权限描述"
          />
        </el-form-item>
        <el-form-item label="资源路径">
          <el-input v-model="permissionForm.resource" placeholder="请输入资源路径" />
        </el-form-item>
        <el-form-item label="操作类型" prop="action">
          <el-select v-model="permissionForm.action" placeholder="请选择操作类型">
            <el-option label="ALL" value="ALL" />
            <el-option label="CREATE" value="CREATE" />
            <el-option label="READ" value="READ" />
            <el-option label="UPDATE" value="UPDATE" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限分类" prop="category">
          <el-select v-model="permissionForm.category" placeholder="请选择权限分类">
            <el-option label="菜单权限" value="MENU" />
            <el-option label="API权限" value="API" />
            <el-option label="按钮权限" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="父权限">
          <el-tree-select
            v-model="permissionForm.parentId"
            :data="permissionTreeOptions"
            check-strictly
            :render-after-expand="false"
            placeholder="请选择父权限"
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            clearable
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="permissionForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="permissionForm.isEnabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ saving ? '保存中...' : '确定' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看权限弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="权限详情"
      width="500px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="权限名称">{{ currentPermission.name }}</el-descriptions-item>
        <el-descriptions-item label="权限编码">{{ currentPermission.code }}</el-descriptions-item>
        <el-descriptions-item label="权限分类">{{ getCategoryText(currentPermission.category) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentPermission.action }}</el-descriptions-item>
        <el-descriptions-item label="资源路径" span="2">{{ currentPermission.resource || '-' }}</el-descriptions-item>
        <el-descriptions-item label="权限描述" span="2">{{ currentPermission.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ currentPermission.sortOrder || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentPermission.isEnabled ? 'success' : 'danger'">
            {{ currentPermission.isEnabled ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" span="2">{{ formatTime(currentPermission.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" span="2">{{ formatTime(currentPermission.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'PermissionManagementView',
  setup() {
    // 响应式数据
    const loading = ref(false)
    const saving = ref(false)
    const permissionList = ref([])
    const selectedPermissions = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const statistics = ref({})
    
    // 搜索表单
    const searchForm = reactive({
      keyword: '',
      category: '',
      isEnabled: null
    })

    // 弹窗相关
    const dialogVisible = ref(false)
    const viewDialogVisible = ref(false)
    const dialogTitle = ref('')
    const currentPermission = ref({})
    const permissionTreeOptions = ref([])

    // 权限表单
    const permissionFormRef = ref(null)
    const permissionForm = reactive({
      id: null,
      name: '',
      code: '',
      description: '',
      resource: '',
      action: '',
      category: '',
      parentId: 0,
      sortOrder: 0,
      isEnabled: true
    })

    // 表单验证规则
    const permissionRules = {
      name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
      code: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
      action: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
      category: [{ required: true, message: '请选择权限分类', trigger: 'change' }]
    }

    // 获取权限统计信息
    const fetchStatistics = async () => {
      try {
        const response = await axios.get('/permission/statistics')
        if (response.data.code === 200) {
          statistics.value = response.data.data
        }
      } catch (error) {
        console.error('获取统计信息失败:', error)
      }
    }

    // 获取权限列表
    const fetchPermissions = async () => {
      loading.value = true
      try {
        const params = {
          keyword: searchForm.keyword,
          category: searchForm.category,
          isEnabled: searchForm.isEnabled,
          pageNum: currentPage.value,
          pageSize: pageSize.value
        }
        
        const response = await axios.get('/permission/list', { params })
        if (response.data.code === 200) {
          const data = response.data.data
          permissionList.value = data.permissions || []
          total.value = data.total || 0
        } else {
          ElMessage.error(response.data.msg || '获取权限列表失败')
        }
      } catch (error) {
        console.error('获取权限列表失败:', error)
        ElMessage.error('获取权限列表失败')
      } finally {
        loading.value = false
      }
    }

    // 获取权限树
    const fetchPermissionTree = async () => {
      try {
        const response = await axios.get('/permission/tree')
        if (response.data.code === 200) {
          permissionTreeOptions.value = [
            { id: 0, name: '根权限', children: response.data.data }
          ]
        }
      } catch (error) {
        console.error('获取权限树失败:', error)
      }
    }

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    }

    // 获取分类文本
    const getCategoryText = (category) => {
      const categoryMap = {
        'MENU': '菜单权限',
        'API': 'API权限',
        'BUTTON': '按钮权限'
      }
      return categoryMap[category] || category
    }

    // 重置表单
    const resetForm = () => {
      if (permissionFormRef.value) {
        permissionFormRef.value.resetFields()
      }
      Object.assign(permissionForm, {
        id: null,
        name: '',
        code: '',
        description: '',
        resource: '',
        action: '',
        category: '',
        parentId: 0,
        sortOrder: 0,
        isEnabled: true
      })
    }

    // 事件处理方法
    const handleSearch = () => {
      currentPage.value = 1
      fetchPermissions()
    }

    const handleReset = () => {
      Object.assign(searchForm, {
        keyword: '',
        category: '',
        isEnabled: null
      })
      handleSearch()
    }

    const handleSelectionChange = (selection) => {
      selectedPermissions.value = selection
    }

    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchPermissions()
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchPermissions()
    }

    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '新建权限'
      dialogVisible.value = true
      fetchPermissionTree()
    }

    const handleEdit = (row) => {
      resetForm()
      Object.assign(permissionForm, { ...row })
      dialogTitle.value = '编辑权限'
      dialogVisible.value = true
      fetchPermissionTree()
    }

    const handleView = (row) => {
      currentPermission.value = { ...row }
      viewDialogVisible.value = true
    }

    const handleSave = async () => {
      if (!permissionFormRef.value) return
      
      try {
        await permissionFormRef.value.validate()
        saving.value = true
        
        const url = permissionForm.id ? `/permission/${permissionForm.id}` : '/permission'
        const method = permissionForm.id ? 'put' : 'post'
        
        const response = await axios[method](url, permissionForm)
        
        if (response.data.code === 200) {
          ElMessage.success(response.data.msg || '操作成功')
          dialogVisible.value = false
          fetchPermissions()
          fetchStatistics()
        } else {
          ElMessage.error(response.data.msg || '操作失败')
        }
      } catch (error) {
        if (error.response?.data?.msg) {
          ElMessage.error(error.response.data.msg)
        } else {
          ElMessage.error('操作失败')
        }
      } finally {
        saving.value = false
      }
    }

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除此权限吗？删除后不可恢复！', '确认删除', {
          type: 'warning'
        })
        
        const response = await axios.delete(`/permission/${row.id}`)
        if (response.data.code === 200) {
          ElMessage.success('删除成功')
          fetchPermissions()
          fetchStatistics()
        } else {
          ElMessage.error(response.data.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    const handleStatusChange = async (row) => {
      try {
        const response = await axios.put(`/permission/${row.id}/status`, null, {
          params: { isEnabled: row.isEnabled }
        })
        
        if (response.data.code === 200) {
          ElMessage.success('状态更新成功')
          fetchStatistics()
        } else {
          ElMessage.error(response.data.msg || '状态更新失败')
          // 恢复原状态
          row.isEnabled = !row.isEnabled
        }
      } catch (error) {
        ElMessage.error('状态更新失败')
        // 恢复原状态
        row.isEnabled = !row.isEnabled
      }
    }

    const handleBatchEnable = async () => {
      try {
        const ids = selectedPermissions.value.map(item => item.id)
        const response = await axios.put('/permission/batch/status', {
          ids,
          isEnabled: true
        })
        
        if (response.data.code === 200) {
          ElMessage.success('批量启用成功')
          fetchPermissions()
          fetchStatistics()
        } else {
          ElMessage.error(response.data.msg || '批量启用失败')
        }
      } catch (error) {
        ElMessage.error('批量启用失败')
      }
    }

    const handleBatchDisable = async () => {
      try {
        const ids = selectedPermissions.value.map(item => item.id)
        const response = await axios.put('/permission/batch/status', {
          ids,
          isEnabled: false
        })
        
        if (response.data.code === 200) {
          ElMessage.success('批量禁用成功')
          fetchPermissions()
          fetchStatistics()
        } else {
          ElMessage.error(response.data.msg || '批量禁用失败')
        }
      } catch (error) {
        ElMessage.error('批量禁用失败')
      }
    }

    const handleBatchDelete = async () => {
      try {
        await ElMessageBox.confirm('确定要删除选中的权限吗？删除后不可恢复！', '确认删除', {
          type: 'warning'
        })
        
        const ids = selectedPermissions.value.map(item => item.id)
        const response = await axios.delete('/permission/batch', { data: ids })
        
        if (response.data.code === 200) {
          ElMessage.success('批量删除成功')
          fetchPermissions()
          fetchStatistics()
        } else {
          ElMessage.error(response.data.msg || '批量删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('批量删除失败')
        }
      }
    }

    // 初始化
    onMounted(() => {
      fetchStatistics()
      fetchPermissions()
    })

    return {
      // 数据
      loading,
      saving,
      permissionList,
      selectedPermissions,
      currentPage,
      pageSize,
      total,
      statistics,
      searchForm,
      dialogVisible,
      viewDialogVisible,
      dialogTitle,
      currentPermission,
      permissionTreeOptions,
      permissionFormRef,
      permissionForm,
      permissionRules,
      
      // 方法
      formatTime,
      getCategoryText,
      resetForm,
      handleSearch,
      handleReset,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleEdit,
      handleView,
      handleSave,
      handleDelete,
      handleStatusChange,
      handleBatchEnable,
      handleBatchDisable,
      handleBatchDelete
    }
  }
}
</script>

<style scoped>
.permission-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-desc {
  color: #666;
  margin: 0;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card.enabled {
  border-left: 4px solid #67c23a;
}

.stat-card.menu {
  border-left: 4px solid #409eff;
}

.stat-card.api {
  border-left: 4px solid #e6a23c;
}

.stat-card.button {
  border-left: 4px solid #f56c6c;
}

.stat-card.disabled {
  border-left: 4px solid #909399;
}

.stat-content {
  padding: 10px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 32px;
  color: #e6e8eb;
}

.main-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.search-area {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.action-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.permission-name {
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-area,
  .action-area {
    justify-content: center;
  }
  
  .stats-section .el-col {
    margin-bottom: 10px;
  }
}
</style> 