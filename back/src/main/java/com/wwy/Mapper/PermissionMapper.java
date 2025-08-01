package com.wwy.Mapper;

import com.wwy.Entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    /**
     * 根据ID获取权限
     */
    Permission getPermissionById(@Param("id") Integer id);

    /**
     * 根据权限编码获取权限
     */
    Permission getPermissionByCode(@Param("code") String code);

    /**
     * 获取所有权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 获取启用的权限列表
     */
    List<Permission> getEnabledPermissions();

    /**
     * 根据父权限ID获取子权限列表
     */
    List<Permission> getPermissionsByParent(@Param("parentId") Integer parentId);

    /**
     * 根据分类获取权限列表
     */
    List<Permission> getPermissionsByCategory(@Param("category") String category);

    /**
     * 搜索权限（支持分页）
     */
    List<Permission> searchPermissions(@Param("keyword") String keyword,
                                     @Param("category") String category,
                                     @Param("isEnabled") Boolean isEnabled,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);

    /**
     * 统计搜索权限数量
     */
    Integer countSearchPermissions(@Param("keyword") String keyword,
                                  @Param("category") String category,
                                  @Param("isEnabled") Boolean isEnabled);

    /**
     * 获取权限树形结构
     */
    List<Permission> getPermissionTree();

    /**
     * 根据角色ID获取权限列表
     */
    List<Permission> getPermissionsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 根据用户ID获取权限列表（通过角色）
     */
    List<Permission> getPermissionsByUserId(@Param("userId") Integer userId);

    /**
     * 插入权限
     */
    int insertPermission(Permission permission);

    /**
     * 更新权限
     */
    int updatePermission(Permission permission);

    /**
     * 删除权限
     */
    int deletePermission(@Param("id") Integer id);

    /**
     * 批量删除权限
     */
    int batchDeletePermissions(@Param("ids") List<Integer> ids);

    /**
     * 更新权限状态
     */
    int updatePermissionStatus(@Param("id") Integer id, @Param("isEnabled") Boolean isEnabled);

    /**
     * 批量更新权限状态
     */
    int batchUpdatePermissionStatus(@Param("ids") List<Integer> ids, @Param("isEnabled") Boolean isEnabled);

    /**
     * 检查权限编码是否存在
     */
    Integer checkPermissionCodeExists(@Param("code") String code, @Param("excludeId") Integer excludeId);

    /**
     * 检查是否有子权限
     */
    Integer countChildPermissions(@Param("parentId") Integer parentId);

    /**
     * 获取权限统计信息
     */
    Integer getTotalPermissionCount();
    Integer getEnabledPermissionCount();
    Integer getDisabledPermissionCount();
    Integer getMenuPermissionCount();
    Integer getApiPermissionCount();
    Integer getButtonPermissionCount();
} 