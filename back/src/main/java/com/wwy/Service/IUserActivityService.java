package com.wwy.Service;

import com.wwy.Entity.UserActivity;

import java.util.List;

/**
 * 用户活动服务接口
 */
public interface IUserActivityService {
    
    /**
     * 记录用户活动
     * @param userActivity 用户活动对象
     * @return 是否成功
     */
    boolean recordActivity(UserActivity userActivity);
    
    /**
     * 记录用户活动（简化方法）
     * @param userId 用户ID
     * @param username 用户名
     * @param actionType 操作类型
     * @param actionDescription 操作描述
     * @return 是否成功
     */
    boolean recordActivity(Integer userId, String username, String actionType, String actionDescription);
    
    /**
     * 记录用户活动（完整方法）
     * @param userId 用户ID
     * @param username 用户名
     * @param actionType 操作类型
     * @param actionDescription 操作描述
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 是否成功
     */
    boolean recordActivity(Integer userId, String username, String actionType, String actionDescription,
                          String targetType, String targetId, String ipAddress, String userAgent);
    
    /**
     * 获取最近的用户活动列表
     * @param limit 限制数量
     * @return 用户活动列表
     */
    List<UserActivity> getRecentActivities(Integer limit);
    
    /**
     * 根据用户ID获取活动列表
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 用户活动列表
     */
    List<UserActivity> getActivitiesByUserId(Integer userId, Integer limit);
    
    /**
     * 根据操作类型获取活动列表
     * @param actionType 操作类型
     * @param limit 限制数量
     * @return 用户活动列表
     */
    List<UserActivity> getActivitiesByActionType(String actionType, Integer limit);
    
    /**
     * 获取活动统计信息
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getActivityStatistics();
    
    /**
     * 清理过期的活动记录
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanOldActivities(Integer days);
} 