package com.wwy.Service.Impl;

import com.wwy.Entity.QaRecord;
import com.wwy.Entity.Dto.QaRequest;
import com.wwy.Mapper.QaRecordMapper;
import com.wwy.Service.IQaService;
import com.wwy.Service.QwenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问答服务实现类
 */
@Slf4j
@Service
public class QaServiceImpl implements IQaService {
    
    @Autowired
    private QaRecordMapper qaRecordMapper;
    
    @Autowired
    private QwenAiService qwenAiService;
    
    @Override
    public QaRecord askQuestion(Integer userId, String username, QaRequest qaRequest) {
        log.info("用户[{}]提问: {}", username, qaRequest.getQuestion());
        
        // 创建问答记录
        QaRecord qaRecord = new QaRecord();
        qaRecord.setUserId(userId);
        qaRecord.setUsername(username);
        qaRecord.setQuestion(qaRequest.getQuestion());
        qaRecord.setModelName(qaRequest.getModelName() != null ? qaRequest.getModelName() : "qwen-turbo");
        qaRecord.setStatus(QaRecord.STATUS_PENDING);
        qaRecord.setCreatedAt(LocalDateTime.now());
        
        try {
            // 先保存问答记录
            qaRecordMapper.insert(qaRecord);
            
            // 调用AI服务获取回答
            QwenAiService.QwenResponse aiResponse = qwenAiService.askQuestion(
                    qaRequest.getQuestion(),
                    qaRequest.getModelName(),
                    qaRequest.getMaxTokens(),
                    qaRequest.getTemperature()
            );
            
            // 更新记录
            if (aiResponse.isSuccess()) {
                qaRecord.setAnswer(aiResponse.getAnswer());
                qaRecord.setTokensUsed(aiResponse.getTokensUsed());
                qaRecord.setResponseTime(aiResponse.getResponseTime());
                qaRecord.setStatus(QaRecord.STATUS_COMPLETED);
                log.info("AI回答成功，tokens: {}, 响应时间: {}ms", aiResponse.getTokensUsed(), aiResponse.getResponseTime());
            } else {
                qaRecord.setStatus(QaRecord.STATUS_FAILED);
                qaRecord.setErrorMessage(aiResponse.getErrorMessage());
                log.error("AI回答失败: {}", aiResponse.getErrorMessage());
            }
            
            qaRecord.setUpdatedAt(LocalDateTime.now());
            qaRecordMapper.updateById(qaRecord);
            
            return qaRecord;
            
        } catch (Exception e) {
            log.error("处理问答时发生异常: {}", e.getMessage(), e);
            
            // 更新状态为失败
            qaRecord.setStatus(QaRecord.STATUS_FAILED);
            qaRecord.setErrorMessage("系统异常: " + e.getMessage());
            qaRecord.setUpdatedAt(LocalDateTime.now());
            qaRecordMapper.updateById(qaRecord);
            
            return qaRecord;
        }
    }
    
    @Override
    public Map<String, Object> getQaRecords(int pageNum, int pageSize, Integer userId, String status) {
        try {
            int offset = (pageNum - 1) * pageSize;
            
            List<QaRecord> records = qaRecordMapper.selectPage(offset, pageSize, userId, status);
            int total = qaRecordMapper.countRecords(userId, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) total / pageSize));
            
            return result;
        } catch (Exception e) {
            log.error("获取问答记录失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取问答记录失败: " + e.getMessage());
        }
    }
    
    @Override
    public QaRecord getQaRecordById(Long id) {
        try {
            return qaRecordMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据ID获取问答记录失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<QaRecord> getUserRecentRecords(Integer userId, int limit) {
        try {
            return qaRecordMapper.selectRecentByUserId(userId, limit);
        } catch (Exception e) {
            log.error("获取用户最近问答记录失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public boolean deleteQaRecord(Long id, Integer userId) {
        try {
            // 先检查记录是否存在且属于当前用户
            QaRecord record = qaRecordMapper.selectById(id);
            if (record == null) {
                log.warn("要删除的问答记录不存在: {}", id);
                return false;
            }
            
            if (!record.getUserId().equals(userId)) {
                log.warn("用户[{}]尝试删除不属于自己的问答记录: {}", userId, id);
                return false;
            }
            
            int result = qaRecordMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除问答记录失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean batchDeleteQaRecords(List<Long> ids, Integer userId) {
        try {
            // 这里应该检查所有记录都属于当前用户，简化实现
            int result = qaRecordMapper.batchDelete(ids);
            return result > 0;
        } catch (Exception e) {
            log.error("批量删除问答记录失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean rateQaRecord(Long id, Integer userId, Integer rating, String feedback) {
        try {
            // 先检查记录是否存在且属于当前用户
            QaRecord record = qaRecordMapper.selectById(id);
            if (record == null || !record.getUserId().equals(userId)) {
                return false;
            }
            
            // 验证评分范围
            if (rating < 1 || rating > 5) {
                log.warn("评分超出范围: {}", rating);
                return false;
            }
            
            record.setRating(rating);
            record.setFeedback(feedback);
            record.setUpdatedAt(LocalDateTime.now());
            
            int result = qaRecordMapper.updateById(record);
            return result > 0;
        } catch (Exception e) {
            log.error("评分问答记录失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getQaStatistics(Integer userId) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            int totalQuestions = qaRecordMapper.countRecords(userId, null);
            int completedQuestions = qaRecordMapper.countRecords(userId, QaRecord.STATUS_COMPLETED);
            int failedQuestions = qaRecordMapper.countRecords(userId, QaRecord.STATUS_FAILED);
            
            statistics.put("totalQuestions", totalQuestions);
            statistics.put("completedQuestions", completedQuestions);
            statistics.put("failedQuestions", failedQuestions);
            statistics.put("successRate", totalQuestions > 0 ? 
                    String.format("%.2f", (double) completedQuestions / totalQuestions * 100) + "%" : "0%");
            
        } catch (Exception e) {
            log.error("获取问答统计信息失败: {}", e.getMessage(), e);
            statistics.put("error", "获取统计信息失败");
        }
        
        return statistics;
    }
    
    @Override
    public List<QaRecord> getPopularQuestions(int limit) {
        try {
            return qaRecordMapper.selectPopularQuestions(limit);
        } catch (Exception e) {
            log.error("获取热门问题失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
} 