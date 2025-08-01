package com.wwy.Entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问答分类实体类
 */
@Data
public class QaCategory {
    
    /**
     * 分类ID
     */
    private Integer id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 分类颜色
     */
    private String color;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 是否启用
     */
    private Boolean isEnabled;
    
    /**
     * 问题数量
     */
    private Integer questionCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 