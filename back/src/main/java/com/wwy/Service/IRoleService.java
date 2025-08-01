package com.wwy.Service;

import com.wwy.Entity.Role;
import com.wwy.Entity.UserRole;

import java.util.List;

public interface IRoleService {
    
    // 根据ID查询角色
    Role getRoleById(Integer id);
    
    // 查询所有角色
    List<Role> getAllRoles();
    
    // 根据角色名查询角色
    Role getRoleByName(String name);
    
    // 根据角色代码查询角色
    Role getRoleByCode(String code);
    
    // 新增角色
    boolean addRole(Role role);
    
    // 更新角色
    boolean updateRole(Role role);
    
    // 删除角色
    boolean deleteRole(Integer id);
    
    // 根据用户ID查询该用户的所有角色
    List<Role> getRolesByUserId(Integer userId);
    
    // 为用户分配角色
    boolean assignRoleToUser(Integer userId, Integer roleId);
    
    // 为用户分配多个角色
    boolean assignRolesToUser(Integer userId, List<Integer> roleIds);
    
    // 移除用户的角色
    boolean removeRoleFromUser(Integer userId, Integer roleId);
    
    // 移除用户的所有角色
    boolean removeAllRolesFromUser(Integer userId);
    
    // 检查用户是否拥有指定角色
    boolean checkUserHasRole(Integer userId, Integer roleId);
    
    // 检查用户是否拥有指定角色代码
    boolean checkUserHasRoleCode(Integer userId, String roleCode);
} 