package com.wwy.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RolePermission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;                    // 关联ID
    private Integer roleId;                // 角色ID
    private Integer permissionId;          // 权限ID
    private LocalDateTime createdAt;       // 创建时间
    
    // 关联对象（非数据库字段）
    private Role role;                     // 角色对象
    private Permission permission;         // 权限对象
    
    // 无参构造方法
    public RolePermission() {}
    
    // 基础构造方法
    public RolePermission(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
    
    // 完整构造方法
    public RolePermission(Integer id, Integer roleId, Integer permissionId, LocalDateTime createdAt) {
        this.id = id;
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.createdAt = createdAt;
    }
    
    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }
    
    @Override
    public String toString() {
        return "RolePermission{" +
                "id=" + id +
                ", roleId=" + roleId +
                ", permissionId=" + permissionId +
                ", createdAt=" + createdAt +
                '}';
    }
} 