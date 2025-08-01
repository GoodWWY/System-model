<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">分类管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增分类</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入分类名称"
            style="width: 200px"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isEnabled" placeholder="请选择状态" style="width: 120px" clearable>
            <el-option label="全部" :value="null"></el-option>
            <el-option label="启用" :value="true"></el-option>
            <el-option label="禁用" :value="false"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedCategories.length > 0">
      <el-alert
        :title="`已选择 ${selectedCategories.length} 个分类`"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      >
        <template #action>
          <el-button size="small" @click="handleBatchEnable">批量启用</el-button>
          <el-button size="small" @click="handleBatchDisable">批量禁用</el-button>
          <el-button size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
        </template>
      </el-alert>
    </div>
    
    <!-- 统计信息 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-number">{{ statistics.totalCount || 0 }}</div>
              <div class="stats-label">总分类数</div>
            </div>
            <el-icon class="stats-icon" color="#409EFF"><FolderOpened /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-number">{{ statistics.enabledCount || 0 }}</div>
              <div class="stats-label">启用分类</div>
            </div>
            <el-icon class="stats-icon" color="#67C23A"><Check /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-number">{{ statistics.disabledCount || 0 }}</div>
              <div class="stats-label">禁用分类</div>
            </div>
            <el-icon class="stats-icon" color="#F56C6C"><Close /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-number">{{ getAverageArticleCount() }}</div>
              <div class="stats-label">平均文章数</div>
            </div>
            <el-icon class="stats-icon" color="#E6A23C"><Document /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="categoryList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150">
          <template #default="{ row }">
            <div class="category-name">
              <el-icon :color="row.color || '#409EFF'" style="margin-right: 5px">
                <component :is="row.icon || 'FolderOpened'" />
              </el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="slug" label="别名" width="120" show-overflow-tooltip />
        <el-table-column prop="articleCount" label="文章数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.articleCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="120" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sortOrder"
              :min="0"
              size="small"
              style="width: 100px"
              :controls="false"
              @blur="handleSortChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isEnabled"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
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
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right"
      />
    </div>
    
    <!-- 分类编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类描述">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        
        <el-form-item label="分类别名">
          <el-input v-model="categoryForm.slug" placeholder="URL友好的别名，留空自动生成" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类图标">
              <el-select v-model="categoryForm.icon" placeholder="选择图标" style="width: 100%">
                <el-option
                  v-for="icon in iconOptions"
                  :key="icon.value"
                  :label="icon.label"
                  :value="icon.value"
                >
                  <el-icon style="margin-right: 8px">
                    <component :is="icon.value" />
                  </el-icon>
                  {{ icon.label }}
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类颜色">
              <el-color-picker v-model="categoryForm.color" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序顺序">
              <el-input-number v-model="categoryForm.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用">
              <el-switch v-model="categoryForm.isEnabled" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 分类查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="分类详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentCategory" class="category-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分类ID">{{ currentCategory.id }}</el-descriptions-item>
          <el-descriptions-item label="分类名称">
            <div class="category-name">
              <el-icon :color="currentCategory.color || '#409EFF'" style="margin-right: 5px">
                <component :is="currentCategory.icon || 'FolderOpened'" />
              </el-icon>
              {{ currentCategory.name }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="分类描述" :span="2">{{ currentCategory.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类别名">{{ currentCategory.slug || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文章数量">
            <el-tag type="info">{{ currentCategory.articleCount || 0 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="排序顺序">{{ currentCategory.sortOrder }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentCategory.isEnabled ? 'success' : 'danger'">
              {{ currentCategory.isEnabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(currentCategory.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(currentCategory.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'CategoryManagementView',
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const categoryList = ref([])
    const selectedCategories = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const statistics = ref({})
    
    // 搜索表单
    const searchForm = reactive({
      keyword: '',
      isEnabled: null
    })
    
    // 对话框相关
    const dialogVisible = ref(false)
    const viewDialogVisible = ref(false)
    const dialogTitle = ref('')
    const currentCategory = ref(null)
    const categoryFormRef = ref(null)
    
    // 分类表单
    const categoryForm = reactive({
      id: null,
      name: '',
      description: '',
      slug: '',
      icon: 'FolderOpened',
      color: '#409EFF',
      sortOrder: 0,
      isEnabled: true
    })
    
    // 表单验证规则
    const categoryRules = {
      name: [
        { required: true, message: '请输入分类名称', trigger: 'blur' }
      ]
    }
    
    // 图标选项
    const iconOptions = [
      { label: '文件夹', value: 'FolderOpened' },
      { label: '文档', value: 'Document' },
      { label: '编辑', value: 'Edit' },
      { label: '设置', value: 'Setting' },
      { label: '工具', value: 'Tools' },
      { label: '星星', value: 'Star' },
      { label: '标签', value: 'Discount' },
      { label: '用户', value: 'User' },
      { label: '位置', value: 'Location' },
      { label: '时间', value: 'Clock' }
    ]
    
    // 获取Token
    const getToken = () => {
      return localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    }
    
    // 获取分类统计信息
    const fetchStatistics = async () => {
      try {
        const res = await axios.get('/category/statistics', {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          statistics.value = res.data.data || {}
        }
      } catch (error) {
        console.error('获取统计信息失败:', error)
      }
    }
    
    // 获取分类列表
    const fetchCategories = async () => {
      loading.value = true
      try {
        const params = {
          pageNum: currentPage.value,
          pageSize: pageSize.value,
          keyword: searchForm.keyword || undefined,
          isEnabled: searchForm.isEnabled
        }
        
        const res = await axios.get('/category/search', {
          headers: { 'Authorization': `Bearer ${getToken()}` },
          params
        })
        
        if (res.data.code === 200) {
          const data = res.data.data
          categoryList.value = data.records || []
          total.value = data.total || 0
          console.log('获取分类列表成功:', categoryList.value)
        } else {
          ElMessage.error(res.data.msg || '获取分类列表失败')
        }
      } catch (error) {
        console.error('获取分类列表失败:', error)
        ElMessage.error('获取分类列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 时间格式化
    const formatTime = (time) => {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    }
    
    // 计算平均文章数
    const getAverageArticleCount = () => {
      if (!categoryList.value.length) return 0
      const totalArticles = categoryList.value.reduce((sum, cat) => sum + (cat.articleCount || 0), 0)
      return Math.round(totalArticles / categoryList.value.length)
    }
    
    // 重置表单
    const resetForm = () => {
      Object.assign(categoryForm, {
        id: null,
        name: '',
        description: '',
        slug: '',
        icon: 'FolderOpened',
        color: '#409EFF',
        sortOrder: 0,
        isEnabled: true
      })
    }
    
    // 事件处理方法
    const handleSearch = () => {
      currentPage.value = 1
      fetchCategories()
    }
    
    const handleReset = () => {
      Object.assign(searchForm, {
        keyword: '',
        isEnabled: null
      })
      currentPage.value = 1
      fetchCategories()
    }
    
    const handleSelectionChange = (selection) => {
      selectedCategories.value = selection
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      fetchCategories()
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchCategories()
    }
    
    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '新增分类'
      dialogVisible.value = true
    }
    
    const handleEdit = (row) => {
      Object.assign(categoryForm, {
        ...row,
        isEnabled: Boolean(row.isEnabled)
      })
      dialogTitle.value = '编辑分类'
      dialogVisible.value = true
    }
    
    const handleView = (row) => {
      currentCategory.value = row
      viewDialogVisible.value = true
    }
    
    const handleSave = async () => {
      if (!categoryFormRef.value) return
      
      categoryFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        saving.value = true
        try {
          const isEdit = !!categoryForm.id
          const url = isEdit ? `/category/${categoryForm.id}` : '/category'
          const method = isEdit ? 'put' : 'post'
          
          const res = await axios[method](url, categoryForm, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
          })
          
          if (res.data.code === 200) {
            ElMessage.success(isEdit ? '更新成功' : '创建成功')
            dialogVisible.value = false
            fetchCategories()
            fetchStatistics()
          } else {
            ElMessage.error(res.data.msg || '保存失败')
          }
        } catch (error) {
          console.error('保存分类失败:', error)
          ElMessage.error('保存失败')
        } finally {
          saving.value = false
        }
      })
    }
    
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const res = await axios.delete(`/category/${row.id}`, {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          fetchCategories()
          fetchStatistics()
        } else {
          ElMessage.error(res.data.msg || '删除失败')
        }
      } catch (error) {
        if (error.message !== 'cancel') {
          console.error('删除分类失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }
    
    const handleStatusChange = async (row) => {
      try {
        const res = await axios.put(`/category/${row.id}/status`, 
          { isEnabled: row.isEnabled }, 
          { headers: { 'Authorization': `Bearer ${getToken()}` } }
        )
        
        if (res.data.code === 200) {
          ElMessage.success(row.isEnabled ? '启用成功' : '禁用成功')
          fetchCategories()
          fetchStatistics()
        } else {
          // 回滚状态
          row.isEnabled = !row.isEnabled
          ElMessage.error(res.data.msg || '状态更新失败')
        }
      } catch (error) {
        // 回滚状态
        row.isEnabled = !row.isEnabled
        console.error('更新状态失败:', error)
        ElMessage.error('状态更新失败')
      }
    }
    
    const handleSortChange = async (row) => {
      try {
        const res = await axios.put(`/category/${row.id}/sort`, 
          { sortOrder: row.sortOrder }, 
          { headers: { 'Authorization': `Bearer ${getToken()}` } }
        )
        
        if (res.data.code === 200) {
          ElMessage.success('排序更新成功')
        } else {
          ElMessage.error(res.data.msg || '排序更新失败')
        }
      } catch (error) {
        console.error('更新排序失败:', error)
        ElMessage.error('排序更新失败')
      }
    }
    
    // 批量操作方法
    const handleBatchEnable = async () => {
      try {
        const ids = selectedCategories.value.map(item => item.id)
        const res = await axios.put('/category/batch/status', 
          { ids, isEnabled: true }, 
          { headers: { 'Authorization': `Bearer ${getToken()}` } }
        )
        
        if (res.data.code === 200) {
          ElMessage.success('批量启用成功')
          fetchCategories()
          fetchStatistics()
        } else {
          ElMessage.error(res.data.msg || '批量启用失败')
        }
      } catch (error) {
        console.error('批量启用失败:', error)
        ElMessage.error('批量启用失败')
      }
    }
    
    const handleBatchDisable = async () => {
      try {
        const ids = selectedCategories.value.map(item => item.id)
        const res = await axios.put('/category/batch/status', 
          { ids, isEnabled: false }, 
          { headers: { 'Authorization': `Bearer ${getToken()}` } }
        )
        
        if (res.data.code === 200) {
          ElMessage.success('批量禁用成功')
          fetchCategories()
          fetchStatistics()
        } else {
          ElMessage.error(res.data.msg || '批量禁用失败')
        }
      } catch (error) {
        console.error('批量禁用失败:', error)
        ElMessage.error('批量禁用失败')
      }
    }
    
    const handleBatchDelete = async () => {
      try {
        await ElMessageBox.confirm(`确定要删除选中的 ${selectedCategories.value.length} 个分类吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const ids = selectedCategories.value.map(item => item.id)
        const res = await axios.delete('/category/batch', {
          headers: { 'Authorization': `Bearer ${getToken()}` },
          data: ids
        })
        
        if (res.data.code === 200) {
          ElMessage.success('批量删除成功')
          fetchCategories()
          fetchStatistics()
        } else {
          ElMessage.error(res.data.msg || '批量删除失败')
        }
      } catch (error) {
        if (error.message !== 'cancel') {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败')
        }
      }
    }
    
    // 初始化
    onMounted(() => {
      fetchCategories()
      fetchStatistics()
    })
    
    return {
      loading,
      saving,
      categoryList,
      selectedCategories,
      currentPage,
      pageSize,
      total,
      statistics,
      searchForm,
      dialogVisible,
      viewDialogVisible,
      dialogTitle,
      currentCategory,
      categoryFormRef,
      categoryForm,
      categoryRules,
      iconOptions,
      formatTime,
      getAverageArticleCount,
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
      handleSortChange,
      handleBatchEnable,
      handleBatchDisable,
      handleBatchDelete
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

.search-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-card {
  position: relative;
  overflow: hidden;
}

.stats-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 80px;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.stats-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 36px;
  opacity: 0.2;
}

.table-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.category-name {
  display: flex;
  align-items: center;
}

.category-detail {
  max-height: 500px;
  overflow-y: auto;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-section {
    padding: 10px;
  }
  
  .search-section .el-form {
    display: block;
  }
  
  .search-section .el-form-item {
    margin-bottom: 15px;
  }
  
  .stats-section .el-col {
    margin-bottom: 15px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    font-size: 12px;
  }
}
</style> 