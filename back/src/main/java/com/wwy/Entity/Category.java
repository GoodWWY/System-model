package com.wwy.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Category implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;                    // 分类ID
    private String name;                   // 分类名称
    private String description;            // 分类描述
    private String slug;                   // 分类别名/URL标识
    private Integer parentId;              // 父分类ID（支持树形结构）
    private Integer sortOrder;             // 排序顺序
    private String icon;                   // 分类图标
    private String color;                  // 分类颜色
    private Boolean isEnabled;             // 是否启用
    private Integer articleCount;          // 文章数量
    private LocalDateTime createdAt;       // 创建时间
    private LocalDateTime updatedAt;       // 更新时间
    
    // 无参构造方法
    public Category() {}
    
    // 有参构造方法
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.slug = name.toLowerCase().replace(" ", "-");
        this.parentId = 0;
        this.sortOrder = 0;
        this.isEnabled = true;
        this.articleCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 全参构造方法
    public Category(Integer id, String name, String description, String slug, Integer parentId, 
                   Integer sortOrder, String icon, String color, Boolean isEnabled, 
                   Integer articleCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.icon = icon;
        this.color = color;
        this.isEnabled = isEnabled;
        this.articleCount = articleCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter和Setter方法
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public Integer getParentId() {
        return parentId;
    }
    
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    public Integer getArticleCount() {
        return articleCount;
    }
    
    public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", slug='" + slug + '\'' +
                ", parentId=" + parentId +
                ", sortOrder=" + sortOrder +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                ", isEnabled=" + isEnabled +
                ", articleCount=" + articleCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 