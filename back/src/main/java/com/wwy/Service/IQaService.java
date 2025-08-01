package com.wwy.Service;

import com.wwy.Entity.QaRecord;
import com.wwy.Entity.Dto.QaRequest;

import java.util.List;
import java.util.Map;

/**
 * 问答服务接口
 */
public interface IQaService {
    
    /**
     * 提交问题并获取AI回答
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param qaRequest 问答请求
     * @return 问答记录
     */
    QaRecord askQuestion(Integer userId, String username, QaRequest qaRequest);
    
    /**
     * 获取问答记录列表
     *
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @param userId 用户ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Map<String, Object> getQaRecords(int pageNum, int pageSize, Integer userId, String status);
    
    /**
     * 根据ID获取问答记录
     *
     * @param id 问答记录ID
     * @return 问答记录
     */
    QaRecord getQaRecordById(Long id);
    
    /**
     * 获取用户最近的问答记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 问答记录列表
     */
    List<QaRecord> getUserRecentRecords(Integer userId, int limit);
    
    /**
     * 删除问答记录
     *
     * @param id 问答记录ID
     * @param userId 用户ID（权限检查）
     * @return 是否成功
     */
    boolean deleteQaRecord(Long id, Integer userId);
    
    /**
     * 批量删除问答记录
     *
     * @param ids 问答记录ID列表
     * @param userId 用户ID（权限检查）
     * @return 是否成功
     */
    boolean batchDeleteQaRecords(List<Long> ids, Integer userId);
    
    /**
     * 对问答记录进行评分
     *
     * @param id 问答记录ID
     * @param userId 用户ID（权限检查）
     * @param rating 评分（1-5星）
     * @param feedback 用户反馈（可选）
     * @return 是否成功
     */
    boolean rateQaRecord(Long id, Integer userId, Integer rating, String feedback);
    
    /**
     * 获取问答统计信息
     *
     * @param userId 用户ID（可选，为空则获取全部统计）
     * @return 统计信息
     */
    Map<String, Object> getQaStatistics(Integer userId);
    
    /**
     * 获取热门问题
     *
     * @param limit 限制数量
     * @return 热门问题列表
     */
    List<QaRecord> getPopularQuestions(int limit);
} 