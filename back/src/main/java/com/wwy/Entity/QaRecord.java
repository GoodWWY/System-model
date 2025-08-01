package com.wwy.Entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问答记录实体类
 */
@Data
public class QaRecord {
    
    /**
     * 问答记录ID
     */
    private Long id;
    
    /**
     * 提问用户ID
     */
    private Integer userId;
    
    /**
     * 提问用户名
     */
    private String username;
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * AI回答
     */
    private String answer;
    
    /**
     * 使用的AI模型
     */
    private String modelName;
    
    /**
     * 消耗的token数量
     */
    private Integer tokensUsed;
    
    /**
     * 响应时间(毫秒)
     */
    private Integer responseTime;
    
    /**
     * 状态：pending-处理中，completed-已完成，failed-失败
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 用户评分：1-5星
     */
    private Integer rating;
    
    /**
     * 用户反馈
     */
    private String feedback;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 状态常量
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    
    // 模型常量
    public static final String MODEL_QWEN_TURBO = "qwen-turbo";
    public static final String MODEL_QWEN_PLUS = "qwen-plus";
    public static final String MODEL_QWEN_MAX = "qwen-max";
    public static final String MODEL_QWEN_MAX_LONGCONTEXT = "qwen-max-longcontext";
} 