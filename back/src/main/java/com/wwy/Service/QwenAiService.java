package com.wwy.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阿里云通义千问AI服务
 */
@Slf4j
@Service
public class QwenAiService {
    
    @Value("${qwen.api-key}")
    private String apiKey;
    
    @Value("${qwen.api-url}")
    private String apiUrl;
    
    private static final String DEFAULT_MODEL = "qwen-turbo";
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 调用通义千问AI获取回答
     *
     * @param question 用户问题
     * @param modelName 使用的模型名称
     * @param maxTokens 最大token数量
     * @param temperature 温度参数
     * @return AI回答结果
     */
    public QwenResponse askQuestion(String question, String modelName, Integer maxTokens, Double temperature) {
        // 检查API密钥
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("通义千问API密钥未配置");
            return QwenResponse.error("AI服务配置错误：API密钥未设置");
        }
        
        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName != null ? modelName : DEFAULT_MODEL);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", question)
            ));
            
            if (maxTokens != null && maxTokens > 0) {
                requestBody.put("max_tokens", maxTokens);
            } else {
                requestBody.put("max_tokens", 1500); // 设置默认值
            }
            
            if (temperature != null && temperature >= 0 && temperature <= 2) {
                requestBody.put("temperature", temperature);
            } else {
                requestBody.put("temperature", 0.7); // 设置默认值
            }
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("调用通义千问AI, 问题: {}, 模型: {}", question, modelName);
            log.debug("请求体: {}", requestBody);
            
            long startTime = System.currentTimeMillis();
            
            // 发送请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.info("通义千问API响应状态: {}, 响应时间: {}ms", response.getStatusCode(), responseTime);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseResponse(response.getBody(), responseTime);
            } else {
                log.error("通义千问API调用失败: {}", response.getStatusCode());
                return QwenResponse.error("API调用失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("调用通义千问AI时发生异常: {}", e.getMessage(), e);
            
            // 根据异常类型返回更具体的错误信息
            String errorMessage;
            if (e.getMessage().contains("Connection refused")) {
                errorMessage = "无法连接到AI服务，请检查网络连接";
            } else if (e.getMessage().contains("timeout")) {
                errorMessage = "AI服务响应超时，请稍后重试";
            } else if (e.getMessage().contains("401")) {
                errorMessage = "AI服务认证失败，请检查API密钥";
            } else if (e.getMessage().contains("429")) {
                errorMessage = "AI服务请求频率过高，请稍后重试";
            } else {
                errorMessage = "调用AI服务时发生异常: " + e.getMessage();
            }
            
            return QwenResponse.error(errorMessage);
        }
    }
    
    /**
     * 解析通义千问API响应
     */
    @SuppressWarnings("unchecked")
    private QwenResponse parseResponse(Map<String, Object> responseBody, long responseTime) {
        try {
            log.debug("解析通义千问API响应: {}", responseBody);
            
            // 检查是否有错误
            if (responseBody.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
                String errorMessage = (String) error.get("message");
                log.error("通义千问API返回错误: {}", errorMessage);
                return QwenResponse.error("AI服务错误: " + errorMessage);
            }
            
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                String content = (String) message.get("content");
                
                // 获取token使用情况
                Map<String, Object> usage = (Map<String, Object>) responseBody.get("usage");
                Integer totalTokens = usage != null ? (Integer) usage.get("total_tokens") : 0;
                
                log.info("AI回答成功, tokens: {}, 响应时间: {}ms", totalTokens, responseTime);
                
                return QwenResponse.success(content, totalTokens, (int) responseTime);
            } else {
                log.error("通义千问API响应格式异常: 没有choices");
                return QwenResponse.error("API响应格式异常：缺少回答内容");
            }
        } catch (Exception e) {
            log.error("解析通义千问API响应时发生异常: {}", e.getMessage(), e);
            return QwenResponse.error("解析API响应时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 通义千问响应封装类
     */
    public static class QwenResponse {
        private boolean success;
        private String answer;
        private String errorMessage;
        private Integer tokensUsed;
        private Integer responseTime;
        
        public static QwenResponse success(String answer, Integer tokensUsed, Integer responseTime) {
            QwenResponse response = new QwenResponse();
            response.success = true;
            response.answer = answer;
            response.tokensUsed = tokensUsed;
            response.responseTime = responseTime;
            return response;
        }
        
        public static QwenResponse error(String errorMessage) {
            QwenResponse response = new QwenResponse();
            response.success = false;
            response.errorMessage = errorMessage;
            return response;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getAnswer() { return answer; }
        public String getErrorMessage() { return errorMessage; }
        public Integer getTokensUsed() { return tokensUsed; }
        public Integer getResponseTime() { return responseTime; }
    }
} 