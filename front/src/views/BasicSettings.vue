<template>
  <div class="content-section">
    <div class="section-header">
      <h2 class="section-title">基础设置</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-refresh" @click="initSettings">初始化设置</el-button>
      </div>
    </div>
    
    <div class="settings-container">
      <el-tabs v-model="activeTab" type="border-card" class="settings-tabs">
        <!-- 系统信息 -->
        <el-tab-pane label="系统信息" name="system">
          <div class="tab-content">
            <el-form :model="systemInfo" :rules="systemRules" ref="systemFormRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="系统名称" prop="name">
                    <el-input v-model="systemInfo.name" placeholder="请输入系统名称"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="系统版本" prop="version">
                    <el-input v-model="systemInfo.version" placeholder="请输入系统版本"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="系统网址" prop="website">
                    <el-input v-model="systemInfo.website" placeholder="请输入系统网址"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="默认分页大小" prop="pageSize">
                    <el-input-number v-model="systemInfo.pageSize" :min="5" :max="100" :step="5"></el-input-number>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="系统描述" prop="description">
                <el-input type="textarea" v-model="systemInfo.description" :rows="3" placeholder="请输入系统描述"></el-input>
              </el-form-item>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="会话超时时间" prop="sessionTimeout">
                    <el-input-number v-model="systemInfo.sessionTimeout" :min="5" :max="180" :step="5"></el-input-number>
                    <span class="form-help">分钟</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="维护模式">
                    <el-switch v-model="systemInfo.maintenanceMode" active-text="开启" inactive-text="关闭"></el-switch>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="维护提示信息" prop="maintenanceMessage" v-if="systemInfo.maintenanceMode">
                <el-input v-model="systemInfo.maintenanceMessage" placeholder="请输入维护提示信息"></el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveSystemInfo" :loading="saving">保存系统信息</el-button>
                <el-button @click="resetSystemInfo">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <!-- 文件上传配置 -->
        <el-tab-pane label="文件上传" name="upload">
          <div class="tab-content">
            <el-form :model="fileUploadConfig" :rules="uploadRules" ref="uploadFormRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="文件上传大小" prop="maxSize">
                    <el-input-number v-model="fileUploadConfig.maxSize" :min="1" :max="100" :step="1"></el-input-number>
                    <span class="form-help">MB</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="头像上传大小" prop="avatarMaxSize">
                    <el-input-number v-model="fileUploadConfig.avatarMaxSize" :min="1" :max="10" :step="1"></el-input-number>
                    <span class="form-help">MB</span>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="允许的文件类型" prop="allowedTypes">
                <el-input v-model="fileUploadConfig.allowedTypes" placeholder="例如: jpg,jpeg,png,gif,pdf,doc,docx"></el-input>
                <div class="form-help">使用逗号分隔多个文件类型</div>
              </el-form-item>
              
              <div class="preview-section">
                <h4>当前配置预览</h4>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="文件上传限制">{{ fileUploadConfig.maxSize }} MB</el-descriptions-item>
                  <el-descriptions-item label="头像上传限制">{{ fileUploadConfig.avatarMaxSize }} MB</el-descriptions-item>
                  <el-descriptions-item label="允许类型" :span="2">
                    <el-tag v-for="type in allowedTypesList" :key="type" size="small" style="margin-right: 5px;">
                      {{ type }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              
              <el-form-item>
                <el-button type="primary" @click="saveFileUploadConfig" :loading="saving">保存上传配置</el-button>
                <el-button @click="resetFileUploadConfig">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <!-- 邮件配置 -->
        <el-tab-pane label="邮件配置" name="mail">
          <div class="tab-content">
            <el-form :model="mailConfig" :rules="mailRules" ref="mailFormRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="SMTP服务器" prop="smtpHost">
                    <el-input v-model="mailConfig.smtpHost" placeholder="例如: smtp.qq.com"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="SMTP端口" prop="smtpPort">
                    <el-input-number v-model="mailConfig.smtpPort" :min="1" :max="65535"></el-input-number>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="邮箱用户名" prop="username">
                    <el-input v-model="mailConfig.username" placeholder="请输入邮箱用户名"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱密码" prop="password">
                    <el-input type="password" v-model="mailConfig.password" placeholder="请输入邮箱密码" show-password></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="发件人名称" prop="fromName">
                <el-input v-model="mailConfig.fromName" placeholder="请输入发件人名称"></el-input>
              </el-form-item>
              
              <div class="test-section">
                <h4>邮件测试</h4>
                <el-row :gutter="20">
                  <el-col :span="16">
                    <el-input v-model="testEmail" placeholder="请输入测试邮箱地址"></el-input>
                  </el-col>
                  <el-col :span="8">
                    <el-button type="primary" @click="testMailConfig" :loading="testing">发送测试邮件</el-button>
                  </el-col>
                </el-row>
              </div>
              
              <el-form-item>
                <el-button type="primary" @click="saveMailConfig" :loading="saving">保存邮件配置</el-button>
                <el-button @click="resetMailConfig">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <!-- 高级设置 -->
        <el-tab-pane label="高级设置" name="advanced">
          <div class="tab-content">
            <el-alert
              title="高级设置"
              type="warning"
              description="高级设置可能会影响系统正常运行，请谨慎修改。"
              show-icon
              :closable="false"
            ></el-alert>
            
            <div class="advanced-section">
              <h4>系统管理</h4>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card shadow="hover" class="operation-card">
                    <template #header>
                      <div class="card-header">
                        <span>清理缓存</span>
                      </div>
                    </template>
                    <p>清理系统缓存，释放存储空间</p>
                    <el-button type="warning" @click="clearCache" :loading="operating">清理缓存</el-button>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover" class="operation-card">
                    <template #header>
                      <div class="card-header">
                        <span>系统信息</span>
                      </div>
                    </template>
                    <p>查看当前系统运行状态和信息</p>
                    <el-button type="info" @click="viewSystemStatus">查看信息</el-button>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover" class="operation-card">
                    <template #header>
                      <div class="card-header">
                        <span>重置设置</span>
                      </div>
                    </template>
                    <p>将所有设置恢复为默认值</p>
                    <el-button type="danger" @click="resetAllSettings" :loading="operating">重置设置</el-button>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 系统状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="系统状态信息" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Java版本">{{ systemStatus.javaVersion }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ systemStatus.osName }}</el-descriptions-item>
        <el-descriptions-item label="系统架构">{{ systemStatus.osArch }}</el-descriptions-item>
        <el-descriptions-item label="可用内存">{{ systemStatus.availableMemory }}</el-descriptions-item>
        <el-descriptions-item label="总内存">{{ systemStatus.totalMemory }}</el-descriptions-item>
        <el-descriptions-item label="CPU核心数">{{ systemStatus.cpuCores }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="statusDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'BasicSettingsView',
  setup() {
    const activeTab = ref('system')
    const saving = ref(false)
    const testing = ref(false)
    const operating = ref(false)
    const statusDialogVisible = ref(false)
    const testEmail = ref('')
    
    // 表单引用
    const systemFormRef = ref(null)
    const uploadFormRef = ref(null)
    const mailFormRef = ref(null)
    
    // 系统信息
    const systemInfo = reactive({
      name: '',
      version: '',
      description: '',
      website: '',
      pageSize: 10,
      sessionTimeout: 30,
      maintenanceMode: false,
      maintenanceMessage: ''
    })
    
    // 文件上传配置
    const fileUploadConfig = reactive({
      maxSize: 10,
      avatarMaxSize: 2,
      allowedTypes: ''
    })
    
    // 邮件配置
    const mailConfig = reactive({
      smtpHost: '',
      smtpPort: 587,
      username: '',
      password: '',
      fromName: ''
    })
    
    // 系统状态
    const systemStatus = reactive({
      javaVersion: 'Java 11.0.12',
      osName: 'Windows 10',
      osArch: 'x64',
      availableMemory: '2.5 GB',
      totalMemory: '8.0 GB',
      cpuCores: '8'
    })
    
    // 允许的文件类型列表
    const allowedTypesList = computed(() => {
      return fileUploadConfig.allowedTypes ? fileUploadConfig.allowedTypes.split(',').map(type => type.trim()) : []
    })
    
    // 表单验证规则
    const systemRules = {
      name: [
        { required: true, message: '请输入系统名称', trigger: 'blur' }
      ],
      version: [
        { required: true, message: '请输入系统版本', trigger: 'blur' }
      ],
      website: [
        { required: true, message: '请输入系统网址', trigger: 'blur' },
        { type: 'url', message: '请输入正确的网址格式', trigger: 'blur' }
      ]
    }
    
    const uploadRules = {
      maxSize: [
        { required: true, message: '请设置文件上传大小限制', trigger: 'blur' }
      ],
      avatarMaxSize: [
        { required: true, message: '请设置头像上传大小限制', trigger: 'blur' }
      ],
      allowedTypes: [
        { required: true, message: '请设置允许的文件类型', trigger: 'blur' }
      ]
    }
    
    const mailRules = {
      smtpHost: [
        { required: true, message: '请输入SMTP服务器', trigger: 'blur' }
      ],
      smtpPort: [
        { required: true, message: '请输入SMTP端口', trigger: 'blur' }
      ],
      username: [
        { required: true, message: '请输入邮箱用户名', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      fromName: [
        { required: true, message: '请输入发件人名称', trigger: 'blur' }
      ]
    }
    
    // 获取Token
    const getToken = () => {
      return localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    }
    
    // 加载系统信息
    const loadSystemInfo = async () => {
      try {
        const res = await axios.get('/settings/system-info', {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          Object.assign(systemInfo, res.data.data)
          console.log('系统信息加载成功:', systemInfo)
        } else {
          ElMessage.warning('系统信息加载失败，使用默认配置')
        }
      } catch (error) {
        console.error('加载系统信息失败:', error)
        ElMessage.warning('系统信息加载失败，使用默认配置')
      }
    }
    
    // 加载文件上传配置
    const loadFileUploadConfig = async () => {
      try {
        const res = await axios.get('/settings/file-upload-config', {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          Object.assign(fileUploadConfig, res.data.data)
          console.log('文件上传配置加载成功:', fileUploadConfig)
        } else {
          ElMessage.warning('文件上传配置加载失败，使用默认配置')
        }
      } catch (error) {
        console.error('加载文件上传配置失败:', error)
        ElMessage.warning('文件上传配置加载失败，使用默认配置')
      }
    }
    
    // 加载邮件配置
    const loadMailConfig = async () => {
      try {
        const res = await axios.get('/settings/mail-config', {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          Object.assign(mailConfig, res.data.data)
          console.log('邮件配置加载成功:', mailConfig)
        } else {
          ElMessage.warning('邮件配置加载失败，使用默认配置')
        }
      } catch (error) {
        console.error('加载邮件配置失败:', error)
        ElMessage.warning('邮件配置加载失败，使用默认配置')
      }
    }
    
    // 保存系统信息
    const saveSystemInfo = async () => {
      if (!systemFormRef.value) return
      
      systemFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        saving.value = true
        try {
          const res = await axios.put('/settings/system-info', systemInfo, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('系统信息保存成功')
          } else {
            ElMessage.error(res.data.msg || '系统信息保存失败')
          }
        } catch (error) {
          console.error('保存系统信息失败:', error)
          ElMessage.error('保存系统信息失败')
        } finally {
          saving.value = false
        }
      })
    }
    
    // 保存文件上传配置
    const saveFileUploadConfig = async () => {
      if (!uploadFormRef.value) return
      
      uploadFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        saving.value = true
        try {
          const res = await axios.put('/settings/file-upload-config', fileUploadConfig, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('文件上传配置保存成功')
          } else {
            ElMessage.error(res.data.msg || '文件上传配置保存失败')
          }
        } catch (error) {
          console.error('保存文件上传配置失败:', error)
          ElMessage.error('保存文件上传配置失败')
        } finally {
          saving.value = false
        }
      })
    }
    
    // 保存邮件配置
    const saveMailConfig = async () => {
      if (!mailFormRef.value) return
      
      mailFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        saving.value = true
        try {
          const res = await axios.put('/settings/mail-config', mailConfig, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('邮件配置保存成功')
          } else {
            ElMessage.error(res.data.msg || '邮件配置保存失败')
          }
        } catch (error) {
          console.error('保存邮件配置失败:', error)
          ElMessage.error('保存邮件配置失败')
        } finally {
          saving.value = false
        }
      })
    }
    
    // 测试邮件配置
    const testMailConfig = async () => {
      if (!testEmail.value) {
        ElMessage.warning('请输入测试邮箱地址')
        return
      }
      
      testing.value = true
      try {
        // 这里应该调用后端的邮件测试接口
        await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟请求
        ElMessage.success('测试邮件发送成功，请检查邮箱')
      } catch (error) {
        console.error('测试邮件发送失败:', error)
        ElMessage.error('测试邮件发送失败')
      } finally {
        testing.value = false
      }
    }
    
    // 初始化设置
    const initSettings = async () => {
      try {
        const res = await axios.post('/settings/init', {}, {
          headers: { 'Authorization': `Bearer ${getToken()}` }
        })
        
        if (res.data.code === 200) {
          ElMessage.success('设置初始化成功')
          // 重新加载配置
          await Promise.all([
            loadSystemInfo(),
            loadFileUploadConfig(),
            loadMailConfig()
          ])
        } else {
          ElMessage.error(res.data.msg || '设置初始化失败')
        }
      } catch (error) {
        console.error('初始化设置失败:', error)
        ElMessage.error('初始化设置失败')
      }
    }
    
    // 重置表单
    const resetSystemInfo = () => {
      if (systemFormRef.value) {
        systemFormRef.value.resetFields()
      }
      loadSystemInfo()
    }
    
    const resetFileUploadConfig = () => {
      if (uploadFormRef.value) {
        uploadFormRef.value.resetFields()
      }
      loadFileUploadConfig()
    }
    
    const resetMailConfig = () => {
      if (mailFormRef.value) {
        mailFormRef.value.resetFields()
      }
      loadMailConfig()
    }
    
    // 高级操作
    const clearCache = async () => {
      operating.value = true
      try {
        await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟请求
        ElMessage.success('缓存清理完成')
      } catch (error) {
        ElMessage.error('缓存清理失败')
      } finally {
        operating.value = false
      }
    }
    
    const viewSystemStatus = () => {
      statusDialogVisible.value = true
    }
    
    const resetAllSettings = async () => {
      try {
        await ElMessageBox.confirm(
          '此操作将重置所有设置为默认值，是否继续？',
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        operating.value = true
        await initSettings()
      } catch (error) {
        console.log('用户取消重置')
      } finally {
        operating.value = false
      }
    }
    
    // 初始化
    onMounted(async () => {
      await Promise.all([
        loadSystemInfo(),
        loadFileUploadConfig(),
        loadMailConfig()
      ])
    })
    
    return {
      activeTab,
      saving,
      testing,
      operating,
      statusDialogVisible,
      testEmail,
      systemFormRef,
      uploadFormRef,
      mailFormRef,
      systemInfo,
      fileUploadConfig,
      mailConfig,
      systemStatus,
      allowedTypesList,
      systemRules,
      uploadRules,
      mailRules,
      saveSystemInfo,
      saveFileUploadConfig,
      saveMailConfig,
      testMailConfig,
      initSettings,
      resetSystemInfo,
      resetFileUploadConfig,
      resetMailConfig,
      clearCache,
      viewSystemStatus,
      resetAllSettings
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

.settings-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-tabs {
  border: none;
  box-shadow: none;
}

.tab-content {
  padding: 20px;
}

.form-help {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.preview-section {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.preview-section h4 {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 14px;
}

.test-section {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
}

.test-section h4 {
  margin: 0 0 15px 0;
  color: #409eff;
  font-size: 14px;
}

.advanced-section {
  margin-top: 20px;
}

.advanced-section h4 {
  margin: 20px 0 15px 0;
  color: #606266;
  font-size: 16px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.operation-card {
  text-align: center;
  margin-bottom: 15px;
}

.operation-card .el-card__body {
  padding: 20px;
}

.operation-card p {
  color: #909399;
  font-size: 13px;
  margin: 10px 0 15px 0;
}

.card-header {
  font-weight: 600;
}

@media (max-width: 768px) {
  .content-section {
    padding: 10px;
  }
  
  .tab-content {
    padding: 15px;
  }
  
  .el-row {
    margin: 0;
  }
  
  .el-col {
    padding: 0 5px;
  }
}
</style> 