<template>
  <div class="qa-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>通义千问 AI 问答</h1>
      <p>基于阿里云通义千问大模型的智能问答助手</p>
    </div>

    <!-- 问答对话区域 -->
    <div class="chat-container">
      <div class="chat-header">
        <div class="chat-title">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 助手</span>
        </div>
        <div class="chat-actions">
          <el-button size="small" @click="clearChat">清空对话</el-button>
          <el-button size="small" type="primary" @click="showHistory = !showHistory">
            {{ showHistory ? '隐藏历史' : '历史记录' }}
          </el-button>
        </div>
      </div>

      <div class="chat-body">
        <!-- 对话记录 -->
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="welcome-message">
            <el-icon size="64"><Robot /></el-icon>
            <h3>欢迎使用通义千问 AI 助手</h3>
            <p>基于阿里云通义千问大模型，为您提供智能问答服务</p>
            
            <!-- 快捷问题 -->
            <div class="quick-questions">
              <h4>热门问题：</h4>
              <div class="question-tags">
                <el-tag 
                  v-for="question in popularQuestions" 
                  :key="question.id" 
                  @click="askQuickQuestion(question.question)"
                  class="question-tag"
                >
                  {{ question.question }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-for="message in messages" :key="message.id" class="message-item">
            <!-- 用户消息 -->
            <div v-if="message.type === 'user'" class="message user-message">
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
              <div class="message-avatar">
                <el-avatar :size="32">{{ username.charAt(0) }}</el-avatar>
              </div>
            </div>

            <!-- AI消息 -->
            <div v-else class="message ai-message">
              <div class="message-avatar">
                <el-avatar :size="32" style="background: #409EFF">
                  <el-icon><Robot /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text" v-html="formatMarkdown(message.content)"></div>
                <div class="message-meta">
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                  <span v-if="message.responseTime" class="response-time">响应时间: {{ message.responseTime }}ms</span>
                  <span v-if="message.tokensUsed" class="tokens-used">消耗Token: {{ message.tokensUsed }}</span>
                </div>
                <!-- 评分区域 -->
                <div v-if="message.status === 'completed'" class="message-rating">
                  <el-rate 
                    v-model="message.rating" 
                    @change="rateMessage(message)"
                    size="small"
                    show-text
                    text-template="{value}星"
                  ></el-rate>
                </div>
              </div>
            </div>

            <!-- 错误消息 -->
            <div v-if="message.type === 'error'" class="message error-message">
              <div class="message-avatar">
                <el-avatar :size="32" style="background: #F56C6C">
                  <el-icon><Warning /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text error-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="isLoading" class="message ai-message loading">
            <div class="message-avatar">
              <el-avatar :size="32" style="background: #409EFF">
                <el-icon class="loading-icon"><Loading /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-text">AI正在思考中...</div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <div class="input-container">
            <el-input
              v-model="currentQuestion"
              type="textarea"
              :rows="3"
              placeholder="请输入您的问题..."
              @keydown.ctrl.enter="sendMessage"
              :disabled="isLoading"
              maxlength="1000"
              show-word-limit
            ></el-input>
            <div class="input-actions">
              <div class="model-selector">
                <el-select v-model="selectedModel" size="small">
                  <el-option label="通义千问-Turbo" value="qwen-turbo"></el-option>
                  <el-option label="通义千问-Plus" value="qwen-plus"></el-option>
                  <el-option label="通义千问-Max" value="qwen-max"></el-option>
                  <el-option label="通义千问-Max长文本" value="qwen-max-longcontext"></el-option>
                </el-select>
              </div>
              <el-button 
                type="primary" 
                @click="sendMessage" 
                :loading="isLoading"
                :disabled="!currentQuestion.trim()"
              >
                <el-icon><Promotion /></el-icon>
                发送 (Ctrl+Enter)
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史记录侧边栏 -->
    <el-drawer
      v-model="showHistory"
      title="历史记录"
      direction="rtl"
      size="400px"
    >
      <div class="history-content">
        <!-- 统计信息 -->
        <el-card class="stats-card" shadow="never">
          <div class="stats-item">
            <span>总问题数</span>
            <span class="stats-value">{{ statistics.totalQuestions || 0 }}</span>
          </div>
          <div class="stats-item">
            <span>成功率</span>
            <span class="stats-value">{{ statistics.successRate || '0%' }}</span>
          </div>
        </el-card>

        <!-- 历史记录列表 -->
        <div class="history-list">
          <div 
            v-for="record in historyRecords" 
            :key="record.id" 
            class="history-item"
            @click="loadHistoryRecord(record)"
          >
            <div class="history-question">{{ truncateText(record.question, 50) }}</div>
            <div class="history-meta">
              <span class="history-time">{{ formatTime(record.createdAt) }}</span>
              <el-tag :type="getStatusType(record.status)" size="small">
                {{ getStatusText(record.status) }}
              </el-tag>
            </div>
          </div>
          
          <!-- 加载更多 -->
          <div v-if="hasMoreHistory" class="load-more">
            <el-button text @click="loadMoreHistory">加载更多</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 响应式数据
const currentQuestion = ref('')
const messages = ref([])
const isLoading = ref(false)
const showHistory = ref(false)
const selectedModel = ref('qwen-turbo')
const messagesContainer = ref(null)
const historyRecords = ref([])
const hasMoreHistory = ref(true)
const historyPage = ref(1)
const statistics = ref({})
const popularQuestions = ref([])

// 用户信息
const username = computed(() => {
  return localStorage.getItem('username') || sessionStorage.getItem('username') || '用户'
})

const userId = computed(() => {
  return parseInt(localStorage.getItem('uid') || sessionStorage.getItem('uid') || '0')
})

// 生命周期
onMounted(() => {
  testConnection()
  loadPopularQuestions()
  loadStatistics()
  loadHistoryRecords()
})

// 测试连接
const testConnection = async () => {
  try {
    console.log('测试后端连接...')
    const response = await axios.get('/test/hello')
    console.log('后端连接成功:', response.data)
  } catch (error) {
    console.error('后端连接失败:', error)
    ElMessage.error('无法连接到后端服务，请检查服务状态')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!currentQuestion.value.trim() || isLoading.value) return

  const question = currentQuestion.value.trim()
  
  // 添加用户消息
  const userMessage = {
    id: Date.now(),
    type: 'user',
    content: question,
    timestamp: new Date()
  }
  messages.value.push(userMessage)
  
  // 清空输入框
  currentQuestion.value = ''
  isLoading.value = true
  
  // 滚动到底部
  scrollToBottom()

  try {
    const response = await axios.post('/qa/ask', {
      question: question,
      modelName: selectedModel.value
    }, {
      headers: {
        'Content-Type': 'application/json',
        'username': username.value,
        'userId': userId.value.toString()
      }
    })

    if (response.data.code === 200) {
      const qaRecord = response.data.data
      
      // 添加AI回复
      const aiMessage = {
        id: qaRecord.id,
        type: qaRecord.status === 'completed' ? 'ai' : 'error',
        content: qaRecord.status === 'completed' ? qaRecord.answer : qaRecord.errorMessage,
        timestamp: new Date(qaRecord.updatedAt),
        status: qaRecord.status,
        responseTime: qaRecord.responseTime,
        tokensUsed: qaRecord.tokensUsed,
        rating: qaRecord.rating || 0
      }
      
      messages.value.push(aiMessage)
    } else {
      // 添加错误消息
      messages.value.push({
        id: Date.now(),
        type: 'error',
        content: response.data.msg || '请求失败',
        timestamp: new Date()
      })
    }
  } catch (error) {
    console.error('发送问题失败:', error)
    let errorMessage = '网络错误，请稍后重试'
    
    if (error.response) {
      // 服务器响应了错误状态码
      errorMessage = error.response.data?.msg || `服务器错误: ${error.response.status}`
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage = '无法连接到服务器，请检查后端服务是否启动'
    } else if (error.message) {
      // 其他错误
      errorMessage = error.message
    }
    
    messages.value.push({
      id: Date.now(),
      type: 'error',
      content: errorMessage,
      timestamp: new Date()
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
    // 刷新历史记录
    loadHistoryRecords(true)
    loadStatistics()
  }
}

// 快捷提问
const askQuickQuestion = (question) => {
  currentQuestion.value = question
  sendMessage()
}

// 清空对话
const clearChat = () => {
  messages.value = []
}

// 加载热门问题
const loadPopularQuestions = async () => {
  try {
    const response = await axios.get('/qa/popular?limit=5')
    
    if (response.data && response.data.code === 200) {
      popularQuestions.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载热门问题失败:', error)
    // 静默失败，不影响主功能
    popularQuestions.value = []
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const response = await axios.get('/qa/statistics', {
      params: { userId: userId.value }
    })
    
    if (response.data && response.data.code === 200) {
      statistics.value = response.data.data || {}
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
    // 静默失败，不影响主功能
    statistics.value = {}
  }
}

// 加载历史记录
const loadHistoryRecords = async (reset = false) => {
  try {
    if (reset) {
      historyPage.value = 1
      historyRecords.value = []
    }
    
    const response = await axios.get('/qa/records', {
      params: {
        pageNum: historyPage.value,
        pageSize: 20,
        userId: userId.value
      }
    })
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data || {}
      const records = data.records || []
      
      if (reset) {
        historyRecords.value = records
      } else {
        historyRecords.value.push(...records)
      }
      hasMoreHistory.value = historyPage.value < (data.totalPages || 0)
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    // 静默失败，不影响主功能
    if (reset) {
      historyRecords.value = []
    }
  }
}

// 加载更多历史记录
const loadMoreHistory = () => {
  historyPage.value++
  loadHistoryRecords()
}

// 加载历史记录到对话
const loadHistoryRecord = (record) => {
  const userMessage = {
    id: record.id + '_user',
    type: 'user',
    content: record.question,
    timestamp: new Date(record.createdAt)
  }
  
  const aiMessage = {
    id: record.id,
    type: record.status === 'completed' ? 'ai' : 'error',
    content: record.status === 'completed' ? record.answer : record.errorMessage,
    timestamp: new Date(record.updatedAt),
    status: record.status,
    responseTime: record.responseTime,
    tokensUsed: record.tokensUsed,
    rating: record.rating || 0
  }
  
  messages.value = [userMessage, aiMessage]
  showHistory.value = false
  scrollToBottom()
}

// 评分消息
const rateMessage = async (message) => {
  try {
    await axios.post('/qa/rate', null, {
      params: {
        id: message.id,
        userId: userId.value,
        rating: message.rating
      }
    })
    
    ElMessage.success('评分成功')
  } catch (error) {
    console.error('评分失败:', error)
    ElMessage.error('评分失败')
  }
}

// 工具函数
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const formatMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
}

const truncateText = (text, maxLength) => {
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const getStatusType = (status) => {
  switch (status) {
    case 'completed': return 'success'
    case 'pending': return 'warning'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'pending': return '处理中'
    case 'failed': return '失败'
    default: return '未知'
  }
}
</script>

<style scoped>
.qa-management {
  padding: 20px;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #fafbfc;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.chat-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 0;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.welcome-message h3 {
  margin: 16px 0 8px 0;
  color: #303133;
}

.quick-questions {
  margin-top: 30px;
}

.quick-questions h4 {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.question-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.question-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.message-item {
  margin-bottom: 24px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.user-message {
  flex-direction: row-reverse;
  margin-left: auto;
}

.user-message .message-content {
  background: #409EFF;
  color: white;
}

.ai-message .message-content {
  background: white;
  border: 1px solid #ebeef5;
}

.error-message .message-content {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  flex: 1;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

.error-text {
  color: #f56c6c;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}

.message-meta {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.7;
}

.message-rating {
  margin-top: 8px;
}

.loading .loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.chat-input {
  padding: 20px;
  border-top: 1px solid #ebeef5;
  background: white;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.model-selector {
  font-size: 12px;
  color: #909399;
}

.history-content {
  padding: 16px 0;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.stats-value {
  font-weight: 600;
  color: #409EFF;
}

.history-list {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.3s;
}

.history-item:hover {
  background: #f5f7fa;
}

.history-question {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}

.history-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.load-more {
  text-align: center;
  padding: 16px;
}
</style> 