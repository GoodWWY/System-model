package com.wwy.Service;

import com.wwy.Entity.Permission;

import java.util.List;
import java.util.Map;

public interface IPermissionService {

    /**
     * 根据ID获取权限
     */
    Permission getPermissionById(Integer id);

    /**
     * 根据权限编码获取权限
     */
    Permission getPermissionByCode(String code);

    /**
     * 获取所有权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 获取启用的权限列表
     */
    List<Permission> getEnabledPermissions();

    /**
     * 获取权限树形结构
     */
    List<Permission> getPermissionTree();

    /**
     * 根据角色ID获取权限列表
     */
    List<Permission> getPermissionsByRoleId(Integer roleId);

    /**
     * 根据用户ID获取权限列表（通过角色）
     */
    List<Permission> getPermissionsByUserId(Integer userId);

    /**
     * 搜索权限（支持分页）
     */
    Map<String, Object> searchPermissions(String keyword, String category, Boolean isEnabled, 
                                        Integer pageNum, Integer pageSize);

    /**
     * 创建权限
     */
    boolean createPermission(Permission permission);

    /**
     * 更新权限
     */
    boolean updatePermission(Permission permission);

    /**
     * 删除权限
     */
    boolean deletePermission(Integer id);

    /**
     * 批量删除权限
     */
    boolean batchDeletePermissions(List<Integer> ids);

    /**
     * 更新权限状态
     */
    boolean updatePermissionStatus(Integer id, Boolean isEnabled);

    /**
     * 批量更新权限状态
     */
    boolean batchUpdatePermissionStatus(List<Integer> ids, Boolean isEnabled);

    /**
     * 检查权限编码是否存在
     */
    boolean checkPermissionCodeExists(String code, Integer excludeId);

    /**
     * 构建权限树形结构
     */
    List<Permission> buildPermissionTree(List<Permission> permissions, Integer parentId);

    /**
     * 获取权限统计信息
     */
    Map<String, Object> getPermissionStatistics();

    /**
     * 为角色分配权限
     */
    boolean assignPermissionsToRole(Integer roleId, List<Integer> permissionIds);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Integer userId, String permissionCode);

    /**
     * 获取用户权限编码列表
     */
    List<String> getUserPermissionCodes(Integer userId);
} 