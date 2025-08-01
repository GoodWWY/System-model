<template>
  <el-drawer
    v-model="drawerVisible"
    title="消息中心"
    direction="rtl"
    size="350px"
  >
    <template #header>
      <div class="drawer-header">
        <h4>消息中心</h4>
        <div class="drawer-header-actions">
          <el-button type="text" @click="markAllAsRead">全部标为已读</el-button>
          <el-button type="text" @click="deleteAllMessages">清空消息</el-button>
        </div>
      </div>
    </template>
    <div class="messages-container">
      <el-tabs v-model="messageActiveTab">
        <el-tab-pane label="全部消息" name="all">
          <div v-if="messageList.length === 0" class="empty-messages">
            <el-icon :size="48"><Bell /></el-icon>
            <p>暂无消息</p>
          </div>
          <div v-else class="message-list">
            <div 
              v-for="(message, index) in messageList" 
              :key="index" 
              class="message-item"
              :class="{'unread': !message.isRead}"
              @click="readMessage(index)"
            >
              <div class="message-avatar">
                <el-avatar :size="40">
                  <el-icon v-if="message.type === 'system'"><Message /></el-icon>
                  <el-icon v-else-if="message.type === 'todo'"><Document /></el-icon>
                  <el-icon v-else><User /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-title">
                  <span>{{ message.title }}</span>
                  <span class="message-time">{{ message.time }}</span>
                </div>
                <div class="message-summary">{{ message.content }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="系统通知" name="system">
          <div v-if="systemMessages.length === 0" class="empty-messages">
            <el-icon :size="48"><Message /></el-icon>
            <p>暂无系统通知</p>
          </div>
          <div v-else class="message-list">
            <div 
              v-for="(message, index) in systemMessages" 
              :key="index" 
              class="message-item"
              :class="{'unread': !message.isRead}"
              @click="readMessage(index, 'system')"
            >
              <div class="message-avatar">
                <el-avatar :size="40">
                  <el-icon><Message /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-title">
                  <span>{{ message.title }}</span>
                  <span class="message-time">{{ message.time }}</span>
                </div>
                <div class="message-summary">{{ message.content }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="待办事项" name="todo">
          <div v-if="todoMessages.length === 0" class="empty-messages">
            <el-icon :size="48"><Document /></el-icon>
            <p>暂无待办事项</p>
          </div>
          <div v-else class="message-list">
            <div 
              v-for="(message, index) in todoMessages" 
              :key="index" 
              class="message-item"
              :class="{'unread': !message.isRead}"
              @click="readMessage(index, 'todo')"
            >
              <div class="message-avatar">
                <el-avatar :size="40">
                  <el-icon><Document /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-title">
                  <span>{{ message.title }}</span>
                  <span class="message-time">{{ message.time }}</span>
                </div>
                <div class="message-summary">{{ message.content }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="messageDetailDialogVisible"
      :title="currentMessage.title"
      width="500px"
    >
      <div class="message-detail">
        <div class="message-detail-header">
          <div class="message-detail-avatar">
            <el-avatar :size="40">
              <el-icon v-if="currentMessage.type === 'system'"><Message /></el-icon>
              <el-icon v-else-if="currentMessage.type === 'todo'"><Document /></el-icon>
              <el-icon v-else><User /></el-icon>
            </el-avatar>
          </div>
          <div class="message-detail-info">
            <div class="message-detail-title">{{ currentMessage.title }}</div>
            <div class="message-detail-time">{{ currentMessage.time }}</div>
          </div>
        </div>
        <div class="message-detail-content">
          {{ currentMessage.content }}
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="messageDetailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleMessageAction">处理</el-button>
        </span>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'MessageCenter',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    unreadCount: {
      type: Number,
      default: 0
    }
  },
  emits: ['update:visible', 'update-count'],
  setup(props, { emit }) {
    const drawerVisible = computed({
      get: () => props.visible,
      set: (value) => emit('update:visible', value)
    })
    
    const messageActiveTab = ref('all')
    const messageDetailDialogVisible = ref(false)
    
    // 消息数据
    const messageList = ref([
      {
        title: '系统更新通知',
        content: '系统将于今晚22:00-23:00进行例行维护，请提前做好相关准备工作。',
        time: '2023-07-28 10:30',
        isRead: false,
        type: 'system'
      },
      {
        title: '待办事项提醒',
        content: '您有一个待办事项即将到期，请尽快处理。',
        time: '2023-07-28 09:15',
        isRead: false,
        type: 'todo'
      },
      {
        title: '用户反馈',
        content: '用户张三提交了一条新的反馈，请及时查看并处理。',
        time: '2023-07-27 16:45',
        isRead: false,
        type: 'user'
      },
      {
        title: '账户安全提醒',
        content: '您的账户今日有3次异常登录尝试，请检查账户安全。',
        time: '2023-07-27 14:20',
        isRead: true,
        type: 'system'
      }
    ])
    
    // 根据类型过滤消息
    const systemMessages = computed(() => {
      return messageList.value.filter(msg => msg.type === 'system')
    })
    
    const todoMessages = computed(() => {
      return messageList.value.filter(msg => msg.type === 'todo')
    })
    
    // 当前选中的消息
    const currentMessage = ref({})
    
    // 阅读消息
    const readMessage = (index, type = 'all') => {
      let targetMessages
      let targetIndex = index
      
      if (type === 'all') {
        targetMessages = messageList.value
      } else if (type === 'system') {
        targetMessages = systemMessages.value
        targetIndex = messageList.value.findIndex(msg => msg === targetMessages[index])
      } else if (type === 'todo') {
        targetMessages = todoMessages.value
        targetIndex = messageList.value.findIndex(msg => msg === targetMessages[index])
      }
      
      // 标记为已读
      if (targetIndex !== -1 && !messageList.value[targetIndex].isRead) {
        messageList.value[targetIndex].isRead = true
        updateUnreadCount()
      }
      
      // 显示消息详情
      currentMessage.value = { ...messageList.value[targetIndex] }
      messageDetailDialogVisible.value = true
    }
    
    // 标记所有消息为已读
    const markAllAsRead = () => {
      messageList.value.forEach(msg => {
        msg.isRead = true
      })
      updateUnreadCount()
      ElMessage.success('已将全部消息标为已读')
    }
    
    // 清空所有消息
    const deleteAllMessages = () => {
      ElMessageBox.confirm('确定要清空所有消息吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        messageList.value = []
        updateUnreadCount()
        ElMessage.success('已清空所有消息')
      }).catch(() => {})
    }
    
    // 更新未读消息数量
    const updateUnreadCount = () => {
      const count = messageList.value.filter(msg => !msg.isRead).length
      emit('update-count', count)
    }
    
    // 处理消息动作
    const handleMessageAction = () => {
      ElMessage.success('消息已处理')
      messageDetailDialogVisible.value = false
    }
    
    // 初始化未读消息计数
    updateUnreadCount()
    
    return {
      drawerVisible,
      messageActiveTab,
      messageDetailDialogVisible,
      messageList,
      systemMessages,
      todoMessages,
      currentMessage,
      readMessage,
      markAllAsRead,
      deleteAllMessages,
      handleMessageAction
    }
  }
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.drawer-header h4 {
  margin: 0;
}

.drawer-header-actions {
  display: flex;
  gap: 10px;
}

.messages-container {
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
  border: 1px solid #ebeef5;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.unread {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.message-title span:first-child {
  font-weight: 600;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-summary {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-detail {
  padding: 20px 0;
}

.message-detail-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.message-detail-info {
  flex: 1;
}

.message-detail-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.message-detail-time {
  font-size: 13px;
  color: #909399;
}

.message-detail-content {
  color: #606266;
  line-height: 1.6;
}
</style> 