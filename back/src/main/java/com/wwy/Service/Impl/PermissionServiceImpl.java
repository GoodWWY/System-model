package com.wwy.Service.Impl;

import com.wwy.Entity.Permission;
import com.wwy.Mapper.PermissionMapper;
import com.wwy.Mapper.RolePermissionMapper;
import com.wwy.Service.IPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements IPermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    @Cacheable(value = "permission", key = "#id")
    public Permission getPermissionById(Integer id) {
        try {
            return permissionMapper.getPermissionById(id);
        } catch (Exception e) {
            logger.error("根据ID获取权限失败，ID: {}", id, e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "permission", key = "'code:' + #code")
    public Permission getPermissionByCode(String code) {
        try {
            return permissionMapper.getPermissionByCode(code);
        } catch (Exception e) {
            logger.error("根据编码获取权限失败，编码: {}", code, e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "permissions", key = "'all'")
    public List<Permission> getAllPermissions() {
        try {
            return permissionMapper.getAllPermissions();
        } catch (Exception e) {
            logger.error("获取所有权限失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "permissions", key = "'enabled'")
    public List<Permission> getEnabledPermissions() {
        try {
            return permissionMapper.getEnabledPermissions();
        } catch (Exception e) {
            logger.error("获取启用权限失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "permissionTree", key = "'tree'")
    public List<Permission> getPermissionTree() {
        try {
            List<Permission> allPermissions = permissionMapper.getPermissionTree();
            return buildPermissionTree(allPermissions, 0);
        } catch (Exception e) {
            logger.error("获取权限树失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "rolePermissions", key = "#roleId")
    public List<Permission> getPermissionsByRoleId(Integer roleId) {
        try {
            return permissionMapper.getPermissionsByRoleId(roleId);
        } catch (Exception e) {
            logger.error("根据角色ID获取权限失败，角色ID: {}", roleId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<Permission> getPermissionsByUserId(Integer userId) {
        try {
            return permissionMapper.getPermissionsByUserId(userId);
        } catch (Exception e) {
            logger.error("根据用户ID获取权限失败，用户ID: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> searchPermissions(String keyword, String category, Boolean isEnabled, 
                                                Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 计算偏移量
            int offset = (pageNum - 1) * pageSize;
            
            // 搜索权限列表
            List<Permission> permissions = permissionMapper.searchPermissions(keyword, category, isEnabled, offset, pageSize);
            
            // 统计总数
            int total = permissionMapper.countSearchPermissions(keyword, category, isEnabled);
            
            result.put("permissions", permissions);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("totalPages", (total + pageSize - 1) / pageSize);
            
        } catch (Exception e) {
            logger.error("搜索权限失败", e);
            result.put("permissions", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"permissions", "permissionTree", "permission"}, allEntries = true)
    public boolean createPermission(Permission permission) {
        try {
            // 检查权限编码是否存在
            if (checkPermissionCodeExists(permission.getCode(), null)) {
                logger.warn("权限编码已存在: {}", permission.getCode());
                return false;
            }
            
            // 设置默认值
            if (permission.getParentId() == null) {
                permission.setParentId(0);
            }
            if (permission.getSortOrder() == null) {
                permission.setSortOrder(0);
            }
            if (permission.getIsEnabled() == null) {
                permission.setIsEnabled(true);
            }
            
            int result = permissionMapper.insertPermission(permission);
            return result > 0;
            
        } catch (Exception e) {
            logger.error("创建权限失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"permissions", "permissionTree", "permission", "rolePermissions", "userPermissions"}, allEntries = true)
    public boolean updatePermission(Permission permission) {
        try {
            // 检查权限编码是否存在（排除自己）
            if (checkPermissionCodeExists(permission.getCode(), permission.getId())) {
                logger.warn("权限编码已存在: {}", permission.getCode());
                return false;
            }
            
            int result = permissionMapper.updatePermission(permission);
            return result > 0;
            
        } catch (Exception e) {
            logger.error("更新权限失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"permissions", "permissionTree", "permission", "rolePermissions", "userPermissions"}, allEntries = true)
    public boolean deletePermission(Integer id) {
        try {
            // 检查是否有子权限
            int childCount = permissionMapper.countChildPermissions(id);
            if (childCount > 0) {
                logger.warn("权限下有子权限，无法删除，权限ID: {}", id);
                return false;
            }
            
            // 先删除角色权限关联
            rolePermissionMapper.deleteRolePermissionsByPermissionId(id);
            
            // 再删除权限
            int result = permissionMapper.deletePermission(id);
            return result > 0;
            
        } catch (Exception e) {
            logger.error("删除权限失败，权限ID: {}", id, e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"permissions", "permissionTree", "permission", "rolePermissions", "userPermissions"}, allEntries = true)
    public boolean batchDeletePermissions(List<Integer> ids) {
        try {
            for (Integer id : ids) {
                if (!deletePermission(id)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("批量删除权限失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"permissions", "permissionTree", "permission", "rolePermissions", "userPermissions"}, allEntries = true)
    public boolean updatePermissionStatus(Integer id, Boolean isEnabled) {
        try {
            int result = permissionMapper.updatePermissionStatus(id, isEnabled);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新权限状态失败，权限ID: {}, 状态: {}", id, isEnabled, e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"permissions", "permissionTree", "permission", "rolePermissions", "userPermissions"}, allEntries = true)
    public boolean batchUpdatePermissionStatus(List<Integer> ids, Boolean isEnabled) {
        try {
            int result = permissionMapper.batchUpdatePermissionStatus(ids, isEnabled);
            return result > 0;
        } catch (Exception e) {
            logger.error("批量更新权限状态失败", e);
            return false;
        }
    }

    @Override
    public boolean checkPermissionCodeExists(String code, Integer excludeId) {
        try {
            int count = permissionMapper.checkPermissionCodeExists(code, excludeId);
            return count > 0;
        } catch (Exception e) {
            logger.error("检查权限编码是否存在失败，编码: {}", code, e);
            return false;
        }
    }

    @Override
    public List<Permission> buildPermissionTree(List<Permission> permissions, Integer parentId) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Permission> tree = new ArrayList<>();
        
        for (Permission permission : permissions) {
            if (Objects.equals(permission.getParentId(), parentId)) {
                // 递归查找子节点
                List<Permission> children = buildPermissionTree(permissions, permission.getId());
                permission.setChildren(children);
                tree.add(permission);
            }
        }
        
        // 按排序字段排序
        tree.sort(Comparator.comparing(Permission::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Permission::getId));
        
        return tree;
    }

    @Override
    @Cacheable(value = "permissionStats", key = "'statistics'")
    public Map<String, Object> getPermissionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("total", permissionMapper.getTotalPermissionCount());
            stats.put("enabled", permissionMapper.getEnabledPermissionCount());
            stats.put("disabled", permissionMapper.getDisabledPermissionCount());
            stats.put("menu", permissionMapper.getMenuPermissionCount());
            stats.put("api", permissionMapper.getApiPermissionCount());
            stats.put("button", permissionMapper.getButtonPermissionCount());
        } catch (Exception e) {
            logger.error("获取权限统计信息失败", e);
            // 返回默认值
            stats.put("total", 0);
            stats.put("enabled", 0);
            stats.put("disabled", 0);
            stats.put("menu", 0);
            stats.put("api", 0);
            stats.put("button", 0);
        }
        return stats;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"rolePermissions", "userPermissions"}, allEntries = true)
    public boolean assignPermissionsToRole(Integer roleId, List<Integer> permissionIds) {
        try {
            // 先删除角色的所有权限
            rolePermissionMapper.deleteRolePermissionsByRoleId(roleId);
            
            // 再添加新的权限
            if (permissionIds != null && !permissionIds.isEmpty()) {
                List<com.wwy.Entity.RolePermission> rolePermissions = permissionIds.stream()
                    .map(permissionId -> new com.wwy.Entity.RolePermission(roleId, permissionId))
                    .collect(Collectors.toList());
                
                int result = rolePermissionMapper.batchInsertRolePermissions(rolePermissions);
                return result > 0;
            }
            return true;
            
        } catch (Exception e) {
            logger.error("为角色分配权限失败，角色ID: {}", roleId, e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Integer userId, String permissionCode) {
        try {
            List<String> userPermissionCodes = getUserPermissionCodes(userId);
            return userPermissionCodes.contains(permissionCode);
        } catch (Exception e) {
            logger.error("检查用户权限失败，用户ID: {}, 权限编码: {}", userId, permissionCode, e);
            return false;
        }
    }

    @Override
    @Cacheable(value = "userPermissionCodes", key = "#userId")
    public List<String> getUserPermissionCodes(Integer userId) {
        try {
            List<Permission> permissions = getPermissionsByUserId(userId);
            return permissions.stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取用户权限编码失败，用户ID: {}", userId, e);
            return new ArrayList<>();
        }
    }
} 