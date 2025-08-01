package com.wwy.Mapper;

import com.wwy.Entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper {

    /**
     * 根据ID获取角色权限关联
     */
    RolePermission getRolePermissionById(@Param("id") Integer id);

    /**
     * 根据角色ID获取权限ID列表
     */
    List<Integer> getPermissionIdsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 根据权限ID获取角色ID列表
     */
    List<Integer> getRoleIdsByPermissionId(@Param("permissionId") Integer permissionId);

    /**
     * 根据角色ID获取角色权限关联列表
     */
    List<RolePermission> getRolePermissionsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 根据权限ID获取角色权限关联列表
     */
    List<RolePermission> getRolePermissionsByPermissionId(@Param("permissionId") Integer permissionId);

    /**
     * 检查角色权限关联是否存在
     */
    Integer checkRolePermissionExists(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    /**
     * 插入角色权限关联
     */
    int insertRolePermission(RolePermission rolePermission);

    /**
     * 批量插入角色权限关联
     */
    int batchInsertRolePermissions(@Param("rolePermissions") List<RolePermission> rolePermissions);

    /**
     * 删除角色权限关联
     */
    int deleteRolePermission(@Param("id") Integer id);

    /**
     * 根据角色ID删除权限关联
     */
    int deleteRolePermissionsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 根据权限ID删除角色关联
     */
    int deleteRolePermissionsByPermissionId(@Param("permissionId") Integer permissionId);

    /**
     * 删除指定角色权限关联
     */
    int deleteRolePermissionByRoleAndPermission(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    /**
     * 批量删除角色权限关联
     */
    int batchDeleteRolePermissions(@Param("ids") List<Integer> ids);

    /**
     * 为角色分配权限（先删除旧的，再插入新的）
     */
    int assignPermissionsToRole(@Param("roleId") Integer roleId, @Param("permissionIds") List<Integer> permissionIds);

    /**
     * 获取角色权限关联统计信息
     */
    Integer getTotalRolePermissionCount();
    Integer getRolePermissionCountByRoleId(@Param("roleId") Integer roleId);
    Integer getRolePermissionCountByPermissionId(@Param("permissionId") Integer permissionId);
} 