package com.wwy.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Permission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;                    // 权限ID
    private String name;                   // 权限名称
    private String code;                   // 权限编码
    private String description;            // 权限描述
    private String resource;               // 资源路径
    private String action;                 // 操作类型(CREATE,READ,UPDATE,DELETE,ALL)
    private String category;               // 权限分类(MENU,BUTTON,API)
    private Integer parentId;              // 父权限ID
    private Integer sortOrder;             // 排序顺序
    private Boolean isEnabled;             // 是否启用
    private LocalDateTime createdAt;       // 创建时间
    private LocalDateTime updatedAt;       // 更新时间
    
    // 非数据库字段
    private List<Permission> children;     // 子权限列表
    private Permission parent;             // 父权限
    private Boolean selected;              // 是否被选中（用于权限树）
    
    // 无参构造方法
    public Permission() {}
    
    // 基础构造方法
    public Permission(String name, String code) {
        this.name = name;
        this.code = code;
    }
    
    // 完整构造方法
    public Permission(Integer id, String name, String code, String description, String resource, 
                     String action, String category, Integer parentId, Integer sortOrder, 
                     Boolean isEnabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.resource = resource;
        this.action = action;
        this.category = category;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.isEnabled = isEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Permission> getChildren() { return children; }
    public void setChildren(List<Permission> children) { this.children = children; }
    
    public Permission getParent() { return parent; }
    public void setParent(Permission parent) { this.parent = parent; }
    
    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
    
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", category='" + category + '\'' +
                ", parentId=" + parentId +
                ", sortOrder=" + sortOrder +
                ", isEnabled=" + isEnabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 