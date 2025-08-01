package com.wwy.Entity.Dto;

import lombok.Data;

/**
 * 问答请求DTO
 */
@Data
public class QaRequest {
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 问题分类ID（可选）
     */
    private Integer categoryId;
    
    /**
     * 使用的AI模型（可选，默认moonshot-v1-8k）
     */
    private String modelName;
    
    /**
     * 最大token数量（可选）
     */
    private Integer maxTokens;
    
    /**
     * 温度参数（可选，控制随机性）
     */
    private Double temperature;
} 