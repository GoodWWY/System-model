package com.wwy.Service.Impl;

import com.wwy.Entity.Role;
import com.wwy.Entity.UserRole;
import com.wwy.Mapper.RoleMapper;
import com.wwy.Mapper.UserRoleMapper;
import com.wwy.Service.IRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements IRoleService {
    
    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Override
    public Role getRoleById(Integer id) {
        return roleMapper.getRoleById(id);
    }
    
    @Override
    public List<Role> getAllRoles() {
        return roleMapper.getAllRoles();
    }
    
    @Override
    public Role getRoleByName(String name) {
        return roleMapper.getRoleByName(name);
    }
    
    @Override
    public Role getRoleByCode(String code) {
        return roleMapper.getRoleByCode(code);
    }
    
    @Override
    @Transactional
    public boolean addRole(Role role) {
        try {
            // 设置创建时间和更新时间
            long currentTime = System.currentTimeMillis();
            role.setCreateTime(currentTime);
            role.setUpdateTime(currentTime);
            
            // 如果状态为空，设置为活跃状态
            if (role.getStatus() == null) {
                role.setStatus("active");
            }
            
            return roleMapper.addRole(role) > 0;
        } catch (Exception e) {
            log.error("添加角色失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateRole(Role role) {
        try {
            // 设置更新时间
            role.setUpdateTime(System.currentTimeMillis());
            return roleMapper.updateRole(role) > 0;
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteRole(Integer id) {
        try {
            // 先删除与该角色相关的用户角色关联
            userRoleMapper.deleteUserRolesByRoleId(id);
            
            // 再删除角色
            return roleMapper.deleteRole(id) > 0;
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return false;
        }
    }
    
    @Override
    public List<Role> getRolesByUserId(Integer userId) {
        return roleMapper.getRolesByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean assignRoleToUser(Integer userId, Integer roleId) {
        try {
            // 检查是否已经分配了该角色
            if (userRoleMapper.checkUserHasRole(userId, roleId) > 0) {
                log.info("用户 {} 已经拥有角色 {}", userId, roleId);
                return true;
            }
            
            // 创建用户角色关联
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(System.currentTimeMillis());
            
            return userRoleMapper.addUserRole(userRole) > 0;
        } catch (Exception e) {
            log.error("为用户分配角色失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean assignRolesToUser(Integer userId, List<Integer> roleIds) {
        try {
            // 先删除用户的所有角色
            userRoleMapper.deleteUserRolesByUserId(userId);
            
            // 如果角色ID列表为空，直接返回成功
            if (roleIds == null || roleIds.isEmpty()) {
                return true;
            }
            
            // 创建用户角色关联列表
            List<UserRole> userRoles = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            
            for (Integer roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(currentTime);
                userRoles.add(userRole);
            }
            
            return userRoleMapper.batchAddUserRole(userRoles) > 0;
        } catch (Exception e) {
            log.error("为用户分配多个角色失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean removeRoleFromUser(Integer userId, Integer roleId) {
        try {
            return userRoleMapper.deleteUserRoleByUserIdAndRoleId(userId, roleId) > 0;
        } catch (Exception e) {
            log.error("移除用户角色失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean removeAllRolesFromUser(Integer userId) {
        try {
            return userRoleMapper.deleteUserRolesByUserId(userId) > 0;
        } catch (Exception e) {
            log.error("移除用户所有角色失败", e);
            return false;
        }
    }
    
    @Override
    public boolean checkUserHasRole(Integer userId, Integer roleId) {
        return userRoleMapper.checkUserHasRole(userId, roleId) > 0;
    }
    
    @Override
    public boolean checkUserHasRoleCode(Integer userId, String roleCode) {
        // 先查找角色代码对应的角色ID
        Role role = roleMapper.getRoleByCode(roleCode);
        if (role == null) {
            return false;
        }
        
        // 检查用户是否拥有该角色
        return checkUserHasRole(userId, role.getId());
    }
} 