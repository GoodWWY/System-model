package com.wwy.Mapper;

import com.wwy.Entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    
    // 添加用户角色关联
    int addUserRole(UserRole userRole);
    
    // 批量添加用户角色关联
    int batchAddUserRole(@Param("userRoles") List<UserRole> userRoles);
    
    // 删除用户角色关联
    int deleteUserRole(@Param("id") Integer id);
    
    // 根据用户ID删除该用户的所有角色关联
    int deleteUserRolesByUserId(@Param("userId") Integer userId);
    
    // 根据角色ID删除该角色的所有用户关联
    int deleteUserRolesByRoleId(@Param("roleId") Integer roleId);
    
    // 根据用户ID和角色ID删除特定的用户角色关联
    int deleteUserRoleByUserIdAndRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
    
    // 查询用户角色关联
    UserRole getUserRoleById(@Param("id") Integer id);
    
    // 根据用户ID查询该用户的所有角色关联
    List<UserRole> getUserRolesByUserId(@Param("userId") Integer userId);
    
    // 根据角色ID查询拥有该角色的所有用户关联
    List<UserRole> getUserRolesByRoleId(@Param("roleId") Integer roleId);
    
    // 检查用户是否拥有特定角色
    int checkUserHasRole(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
} 