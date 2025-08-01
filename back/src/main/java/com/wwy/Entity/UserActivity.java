package com.wwy.Entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户活动实体类
 */
@Data
public class UserActivity {
    
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作类型
     */
    private String actionType;
    
    /**
     * 操作描述
     */
    private String actionDescription;
    
    /**
     * 目标类型
     */
    private String targetType;
    
    /**
     * 目标ID
     */
    private String targetId;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 状态(1:成功, 0:失败)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    // 常用的操作类型常量
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_REGISTER = "REGISTER";
    public static final String ACTION_UPDATE_PROFILE = "UPDATE_PROFILE";
    public static final String ACTION_UPLOAD_AVATAR = "UPLOAD_AVATAR";
    public static final String ACTION_CREATE_USER = "CREATE_USER";
    public static final String ACTION_UPDATE_USER = "UPDATE_USER";
    public static final String ACTION_DELETE_USER = "DELETE_USER";
    public static final String ACTION_CREATE_ROLE = "CREATE_ROLE";
    public static final String ACTION_UPDATE_ROLE = "UPDATE_ROLE";
    public static final String ACTION_DELETE_ROLE = "DELETE_ROLE";
    public static final String ACTION_CREATE_ARTICLE = "CREATE_ARTICLE";
    public static final String ACTION_UPDATE_ARTICLE = "UPDATE_ARTICLE";
    public static final String ACTION_DELETE_ARTICLE = "DELETE_ARTICLE";
    public static final String ACTION_SYSTEM_BACKUP = "SYSTEM_BACKUP";
    public static final String ACTION_SYSTEM_CONFIG = "SYSTEM_CONFIG";
    
    // 目标类型常量
    public static final String TARGET_USER = "user";
    public static final String TARGET_ROLE = "role";
    public static final String TARGET_ARTICLE = "article";
    public static final String TARGET_SYSTEM = "system";
} 