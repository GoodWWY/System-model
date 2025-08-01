package com.wwy.Mapper;

import com.wwy.Entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    
    // 根据ID查询角色
    Role getRoleById(@Param("id") Integer id);
    
    // 查询所有角色
    List<Role> getAllRoles();
    
    // 根据角色名查询角色
    Role getRoleByName(@Param("name") String name);
    
    // 根据角色代码查询角色
    Role getRoleByCode(@Param("code") String code);
    
    // 新增角色
    int addRole(Role role);
    
    // 更新角色
    int updateRole(Role role);
    
    // 删除角色
    int deleteRole(@Param("id") Integer id);
    
    // 根据用户ID查询该用户的所有角色
    List<Role> getRolesByUserId(@Param("userId") Integer userId);
} 