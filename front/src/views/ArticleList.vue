<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">文章管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增文章</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入标题、内容关键词"
            style="width: 200px"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 120px" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="草稿" value="draft"></el-option>
            <el-option label="已发布" value="published"></el-option>
            <el-option label="已归档" value="archived"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" style="width: 150px" clearable>
            <el-option label="全部" :value="null"></el-option>
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedArticles.length > 0">
      <el-alert
        :title="`已选择 ${selectedArticles.length} 篇文章`"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      >
        <template #action>
          <el-button size="small" @click="handleBatchPublish">批量发布</el-button>
          <el-button size="small" @click="handleBatchUnpublish">批量下线</el-button>
          <el-button size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
        </template>
      </el-alert>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="articleList"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="article-title">
              <el-tag v-if="row.isTop" type="danger" size="small" style="margin-right: 5px">置顶</el-tag>
              <el-tag v-if="row.isRecommend" type="warning" size="small" style="margin-right: 5px">推荐</el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.categoryName" size="small">{{ row.categoryName }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读量" width="100" />
        <el-table-column prop="likeCount" label="点赞数" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="{ row }">
            {{ row.publishTime ? formatTime(row.publishTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-dropdown @command="(command) => handleDropdownCommand(command, row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="row.status !== 'published'" command="publish">发布</el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 'published'" command="unpublish">下线</el-dropdown-item>
                  <el-dropdown-item command="archive">归档</el-dropdown-item>
                  <el-dropdown-item :command="row.isTop ? 'untop' : 'top'">
                    {{ row.isTop ? '取消置顶' : '设为置顶' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="row.isRecommend ? 'unrecommend' : 'recommend'">
                    {{ row.isRecommend ? '取消推荐' : '设为推荐' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" style="color: #f56c6c">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
    
    <!-- 文章编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="80%"
      :close-on-click-modal="false"
    >
      <el-form :model="articleForm" :rules="articleRules" ref="articleFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="文章分类" prop="categoryId">
              <el-select v-model="articleForm.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="文章摘要">
          <el-input
            v-model="articleForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要"
          />
        </el-form-item>
        
        <el-form-item label="文章标签">
          <el-input v-model="articleForm.tags" placeholder="请输入标签，用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="文章内容" prop="content">
          <el-input
            v-model="articleForm.content"
            type="textarea"
            :rows="15"
            placeholder="请输入文章内容"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="文章状态">
              <el-select v-model="articleForm.status" placeholder="请选择状态">
                <el-option label="草稿" value="draft" />
                <el-option label="发布" value="published" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否置顶">
              <el-switch v-model="articleForm.isTop" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否推荐">
              <el-switch v-model="articleForm.isRecommend" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 文章查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="文章详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="currentArticle" class="article-detail">
        <div class="article-header">
          <h2>{{ currentArticle.title }}</h2>
          <div class="article-meta">
            <el-tag v-if="currentArticle.categoryName" type="primary">{{ currentArticle.categoryName }}</el-tag>
            <el-tag :type="getStatusType(currentArticle.status)">{{ getStatusText(currentArticle.status) }}</el-tag>
            <span>作者：{{ currentArticle.authorName }}</span>
            <span>阅读量：{{ currentArticle.viewCount }}</span>
            <span>点赞数：{{ currentArticle.likeCount }}</span>
            <span>创建时间：{{ formatTime(currentArticle.createdAt) }}</span>
          </div>
        </div>
        
        <div class="article-summary" v-if="currentArticle.summary">
          <h4>文章摘要</h4>
          <p>{{ currentArticle.summary }}</p>
        </div>
        
        <div class="article-tags" v-if="currentArticle.tags">
          <h4>文章标签</h4>
          <el-tag
            v-for="tag in currentArticle.tags.split(',')"
            :key="tag"
            style="margin-right: 5px"
          >
            {{ tag.trim() }}
          </el-tag>
        </div>
        
        <div class="article-content">
          <h4>文章内容</h4>
          <div class="content-text">{{ currentArticle.content }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'ArticleListView',
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const articleList = ref([])
    const categories = ref([])
    const selectedArticles = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    // 搜索表单
    const searchForm = reactive({
      keyword: '',
      status: '',
      categoryId: null
    })
    
    // 对话框相关
    const dialogVisible = ref(false)
    const viewDialogVisible = ref(false)
    const dialogTitle = ref('')
    const currentArticle = ref(null)
    const articleFormRef = ref(null)
    
    // 文章表单
    const articleForm = reactive({
      id: null,
      title: '',
      content: '',
      summary: '',
      categoryId: null,
      tags: '',
      status: 'draft',
      isTop: false,
      isRecommend: false,
      authorId: 1, // 当前登录用户ID
      authorName: '管理员' // 当前登录用户名
    })
    
    // 表单验证规则
    const articleRules = {
      title: [
        { required: true, message: '请输入文章标题', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入文章内容', trigger: 'blur' }
      ]
    }
    
    // 获取Token
    const getToken = () => {
      return localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    }
    
    // 获取分类列表
    const fetchCategories = async () => {
      try {
        const res = await axios.get('/category/enabled', {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          categories.value = res.data.data || []
        }
      } catch (error) {
        console.error('获取分类列表失败:', error)
      }
    }
    
    // 获取文章列表
    const fetchArticles = async () => {
      loading.value = true
      try {
        const params = {
          pageNum: currentPage.value,
          pageSize: pageSize.value,
          keyword: searchForm.keyword || undefined,
          status: searchForm.status || undefined,
          categoryId: searchForm.categoryId || undefined
        }
        
        const res = await axios.get('/article/search', {
          headers: { 'Authorization': `Bearer ${getToken()}` },
          params
        })
        
        if (res.data.code === 200) {
          const data = res.data.data
          articleList.value = data.records || []
          total.value = data.total || 0
          console.log('获取文章列表成功:', articleList.value)
        } else {
          ElMessage.error(res.data.msg || '获取文章列表失败')
        }
      } catch (error) {
        console.error('获取文章列表失败:', error)
        ElMessage.error('获取文章列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 状态相关方法
    const getStatusType = (status) => {
      const statusMap = {
        'draft': 'info',
        'published': 'success',
        'archived': 'warning'
      }
      return statusMap[status] || 'info'
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        'draft': '草稿',
        'published': '已发布',
        'archived': '已归档'
      }
      return statusMap[status] || '未知'
    }
    
    // 时间格式化
    const formatTime = (time) => {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    }
    
    // 重置表单
    const resetForm = () => {
      Object.assign(articleForm, {
        id: null,
        title: '',
        content: '',
        summary: '',
        categoryId: null,
        tags: '',
        status: 'draft',
        isTop: false,
        isRecommend: false,
        authorId: 1,
        authorName: '管理员'
      })
    }
    
    // 事件处理方法
    const handleSearch = () => {
      currentPage.value = 1
      fetchArticles()
    }
    
    const handleReset = () => {
      Object.assign(searchForm, {
        keyword: '',
        status: '',
        categoryId: null
      })
      currentPage.value = 1
      fetchArticles()
    }
    
    const handleSelectionChange = (selection) => {
      selectedArticles.value = selection
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      fetchArticles()
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchArticles()
    }
    
    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '新增文章'
      dialogVisible.value = true
    }
    
    const handleEdit = (row) => {
      Object.assign(articleForm, {
        ...row,
        isTop: Boolean(row.isTop),
        isRecommend: Boolean(row.isRecommend)
      })
      dialogTitle.value = '编辑文章'
      dialogVisible.value = true
    }
    
    const handleView = (row) => {
      currentArticle.value = row
      viewDialogVisible.value = true
    }
    
    const handleSave = async () => {
      if (!articleFormRef.value) return
      
      articleFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        saving.value = true
        try {
          const isEdit = !!articleForm.id
          const url = isEdit ? `/article/${articleForm.id}` : '/article'
          const method = isEdit ? 'put' : 'post'
          
          const res = await axios[method](url, articleForm, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
          })
          
          if (res.data.code === 200) {
            ElMessage.success(isEdit ? '更新成功' : '创建成功')
            dialogVisible.value = false
            fetchArticles()
          } else {
            ElMessage.error(res.data.msg || '保存失败')
          }
        } catch (error) {
          console.error('保存文章失败:', error)
          ElMessage.error('保存失败')
        } finally {
          saving.value = false
        }
      })
    }
    
    const handleDropdownCommand = async (command, row) => {
      try {
        let res
        switch (command) {
          case 'publish':
            res = await axios.put(`/article/${row.id}/publish`, {}, {
              headers: { 'Authorization': `Bearer ${getToken()}` }
            })
            break
          case 'unpublish':
            res = await axios.put(`/article/${row.id}/unpublish`, {}, {
              headers: { 'Authorization': `Bearer ${getToken()}` }
            })
            break
          case 'archive':
            res = await axios.put(`/article/${row.id}/archive`, {}, {
              headers: { 'Authorization': `Bearer ${getToken()}` }
            })
            break
          case 'top':
          case 'untop':
            res = await axios.put(`/article/${row.id}/top`, { isTop: command === 'top' }, {
              headers: { 'Authorization': `Bearer ${getToken()}` }
            })
            break
          case 'recommend':
          case 'unrecommend':
            res = await axios.put(`/article/${row.id}/recommend`, { isRecommend: command === 'recommend' }, {
              headers: { 'Authorization': `Bearer ${getToken()}` }
            })
            break
          case 'delete':
            await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            })
            res = await axios.delete(`/article/${row.id}`, {
              headers: { 'Authorization': `Bearer ${getToken()}` }
            })
            break
        }
        
        if (res && res.data.code === 200) {
          ElMessage.success('操作成功')
          fetchArticles()
        } else {
          ElMessage.error(res?.data?.msg || '操作失败')
        }
      } catch (error) {
        if (error.message !== 'cancel') {
          console.error('操作失败:', error)
          ElMessage.error('操作失败')
        }
      }
    }
    
    // 批量操作方法
    const handleBatchPublish = async () => {
      try {
        const ids = selectedArticles.value.map(item => item.id)
        const res = await axios.put('/article/batch/status', { ids, status: 'published' }, {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          ElMessage.success('批量发布成功')
          fetchArticles()
        } else {
          ElMessage.error(res.data.msg || '批量发布失败')
        }
      } catch (error) {
        console.error('批量发布失败:', error)
        ElMessage.error('批量发布失败')
      }
    }
    
    const handleBatchUnpublish = async () => {
      try {
        const ids = selectedArticles.value.map(item => item.id)
        const res = await axios.put('/article/batch/status', { ids, status: 'draft' }, {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          ElMessage.success('批量下线成功')
          fetchArticles()
        } else {
          ElMessage.error(res.data.msg || '批量下线失败')
        }
      } catch (error) {
        console.error('批量下线失败:', error)
        ElMessage.error('批量下线失败')
      }
    }
    
    const handleBatchDelete = async () => {
      try {
        await ElMessageBox.confirm(`确定要删除选中的 ${selectedArticles.value.length} 篇文章吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const ids = selectedArticles.value.map(item => item.id)
        const res = await axios.delete('/article/batch', {
          headers: { 'Authorization': `Bearer ${getToken()}` },
          data: ids
        })
        
        if (res.data.code === 200) {
          ElMessage.success('批量删除成功')
          fetchArticles()
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
      fetchArticles()
    })
    
    return {
      loading,
      saving,
      articleList,
      categories,
      selectedArticles,
      currentPage,
      pageSize,
      total,
      searchForm,
      dialogVisible,
      viewDialogVisible,
      dialogTitle,
      currentArticle,
      articleFormRef,
      articleForm,
      articleRules,
      getStatusType,
      getStatusText,
      formatTime,
      handleSearch,
      handleReset,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleEdit,
      handleView,
      handleSave,
      handleDropdownCommand,
      handleBatchPublish,
      handleBatchUnpublish,
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

.table-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.article-title {
  display: flex;
  align-items: center;
}

.article-detail {
  max-height: 600px;
  overflow-y: auto;
}

.article-header h2 {
  margin: 0 0 15px 0;
  color: #2c3e50;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 20px;
}

.article-meta span {
  color: #666;
  font-size: 14px;
}

.article-summary,
.article-tags,
.article-content {
  margin-bottom: 20px;
}

.article-summary h4,
.article-tags h4,
.article-content h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 16px;
}

.content-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #333;
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
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style> 