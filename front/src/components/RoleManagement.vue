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
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <div class="action-buttons">
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
          <el-input v-model="addRoleForm.code" placeholder="大写字母和下划线，如 ROLE_ADMIN"></el-input>
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
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'RoleManagementView',
  setup() {
    // 角色表格数据
    const roleTableData = ref([]);
    const roleTableTotal = ref(0);
    
    // 添加角色相关
    const addRoleDialogVisible = ref(false);
    const addRoleFormRef = ref(null);
    const addRoleForm = reactive({
      name: '',
      code: '',
      description: '',
      status: true
    });
    
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
    };
    
    // 编辑角色相关
    const editRoleDialogVisible = ref(false);
    const editRoleFormRef = ref(null);
    const editRoleForm = reactive({
      id: null,
      name: '',
      code: '',
      description: '',
      status: true
    });
    
    const editRoleRules = {
      name: [
        { required: true, message: '请输入角色名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ]
    };
    
    // 获取角色列表
    const fetchRoleList = async () => {
      console.log('开始获取角色列表...');
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
        console.log('使用的token:', token);
        
        const res = await axios.get('/role/list', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        console.log('角色列表API响应:', res.data);
        
        if (res.data.code === 200) {
          roleTableData.value = res.data.data;
          roleTableTotal.value = res.data.data.length;
          console.log('获取到的角色列表:', roleTableData.value);
        } else {
          ElMessage.error('获取角色列表失败');
          console.error('获取角色列表失败:', res.data.msg);
        }
      } catch (error) {
        console.error('获取角色列表失败:', error);
        if (error.response) {
          console.error('错误响应:', error.response.data);
          console.error('错误状态码:', error.response.status);
        }
        
        // 模拟数据
        roleTableData.value = [
          {id: 1, name: '管理员', code: 'ROLE_ADMIN', description: '系统管理员，拥有所有权限', status: 'active'},
          {id: 2, name: '普通用户', code: 'ROLE_USER', description: '普通用户，拥有基本权限', status: 'active'},
          {id: 3, name: '访客', code: 'ROLE_GUEST', description: '访客用户，只有查看权限', status: 'inactive'}
        ];
        roleTableTotal.value = roleTableData.value.length;
      }
    };
    
    // 打开添加角色对话框
    const openAddRoleDialog = () => {
      addRoleForm.name = '';
      addRoleForm.code = '';
      addRoleForm.description = '';
      addRoleForm.status = true;
      
      addRoleDialogVisible.value = true;
      
      if (addRoleFormRef.value) {
        addRoleFormRef.value.resetFields();
      }
    };
    
    // 提交添加角色
    const submitAddRole = () => {
      if (!addRoleFormRef.value) {
        console.error('添加角色表单引用不存在');
        return;
      }
      
      addRoleFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const roleData = {
              name: addRoleForm.name,
              code: addRoleForm.code,
              description: addRoleForm.description,
              status: addRoleForm.status ? 'active' : 'inactive'
            };
            
            const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
            const res = await axios.post('/role/add', roleData, {
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              }
            });
            
            if (res.data.code === 200) {
              ElMessage.success('添加角色成功');
              addRoleDialogVisible.value = false;
              fetchRoleList();
              // 如果有角色下拉框，还需要更新角色选项
              // fetchRoles(); 
            } else {
              ElMessage.error(res.data.msg || '添加角色失败');
            }
          } catch (error) {
            console.error('添加角色失败:', error);
            ElMessage.error('添加角色失败: ' + (error.message || '未知错误'));
          }
        }
      });
    };
    
    // 处理编辑角色
    const handleEditRole = (role) => {
      // 先设置表单数据，然后再显示对话框，确保表单数据已经更新
      editRoleForm.id = role.id;
      editRoleForm.name = role.name;
      editRoleForm.code = role.code;
      editRoleForm.description = role.description || '';
      editRoleForm.status = role.status === 'active';
      
      // 显示编辑对话框
      editRoleDialogVisible.value = true;
      
      console.log('编辑角色:', role.id, role.name, role.code);
    };
    
    // 提交编辑角色
    const submitEditRole = () => {
      if (!editRoleFormRef.value) {
        console.error('编辑角色表单引用不存在');
        return;
      }
      
      console.log('正在提交编辑角色表单:', JSON.stringify(editRoleForm));
      
      editRoleFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const roleData = {
              id: editRoleForm.id,
              name: editRoleForm.name,
              code: editRoleForm.code,
              description: editRoleForm.description,
              status: editRoleForm.status ? 'active' : 'inactive'
            };
            
            const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
            const res = await axios.put('/role/update', roleData, {
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              }
            });
            
            if (res.data.code === 200) {
              ElMessage.success('更新角色成功');
              editRoleDialogVisible.value = false;
              fetchRoleList();
              // 如果有角色下拉框，还需要更新角色选项
              // fetchRoles();
            } else {
              ElMessage.error(res.data.msg || '更新角色失败');
            }
          } catch (error) {
            console.error('更新角色失败:', error);
            ElMessage.error('更新角色失败: ' + (error.message || '未知错误'));
          }
        }
      });
    };
    
    // 处理删除角色
    const handleDeleteRole = (role) => {
      if (role.code === 'ROLE_ADMIN') {
        ElMessage.warning('管理员角色不能删除');
        return;
      }
      
      ElMessageBox.confirm(`确认删除角色 ${role.name}?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
          const res = await axios.delete(`/role/delete/${role.id}`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          if (res.data.code === 200) {
            ElMessage.success('删除角色成功');
            fetchRoleList();
            // 如果有角色下拉框，还需要更新角色选项
            // fetchRoles();
          } else {
            ElMessage.error(res.data.msg || '删除角色失败');
          }
        } catch (error) {
          console.error('删除角色失败:', error);
          ElMessage.error('删除角色失败: ' + (error.message || '未知错误'));
        }
      }).catch(() => {
        // 取消删除
      });
    };
    
    // 初始化角色
    const initRoles = async () => {
      try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token') || '';
        const res = await axios.post('/role/init', {}, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (res.data.code === 200) {
          ElMessage.success('初始化角色成功');
          fetchRoleList();
          // 如果有角色下拉框，还需要更新角色选项
          // fetchRoles();
        } else {
          ElMessage.error(res.data.msg || '初始化角色失败');
        }
      } catch (error) {
        console.error('初始化角色失败:', error);
        ElMessage.error('初始化角色失败: ' + (error.message || '未知错误'));
      }
    };
    
    onMounted(() => {
      fetchRoleList();
    });
    
    return {
      roleTableData,
      roleTableTotal,
      addRoleDialogVisible,
      addRoleFormRef,
      addRoleForm,
      addRoleRules,
      editRoleDialogVisible,
      editRoleFormRef,
      editRoleForm,
      editRoleRules,
      openAddRoleDialog,
      submitAddRole,
      handleEditRole,
      submitEditRole,
      handleDeleteRole,
      initRoles,
      fetchRoleList
    };
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

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
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