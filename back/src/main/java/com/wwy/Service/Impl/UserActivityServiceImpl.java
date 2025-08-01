package com.wwy.Service.Impl;

import com.wwy.Entity.UserActivity;
import com.wwy.Mapper.UserActivityMapper;
import com.wwy.Service.IUserActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户活动服务实现类
 */
@Slf4j
@Service
public class UserActivityServiceImpl implements IUserActivityService {
    
    @Autowired
    private UserActivityMapper userActivityMapper;
    
    @Override
    public boolean recordActivity(UserActivity userActivity) {
        try {
            if (userActivity.getCreatedAt() == null) {
                userActivity.setCreatedAt(LocalDateTime.now());
            }
            if (userActivity.getStatus() == null) {
                userActivity.setStatus(1); // 默认成功状态
            }
            
            int result = userActivityMapper.insert(userActivity);
            
            if (result > 0) {
                log.info("用户活动记录成功: 用户[{}] 执行了 [{}] 操作", 
                        userActivity.getUsername(), userActivity.getActionDescription());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("记录用户活动失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean recordActivity(Integer userId, String username, String actionType, String actionDescription) {
        UserActivity activity = new UserActivity();
        activity.setUserId(userId);
        activity.setUsername(username);
        activity.setActionType(actionType);
        activity.setActionDescription(actionDescription);
        
        return recordActivity(activity);
    }
    
    @Override
    public boolean recordActivity(Integer userId, String username, String actionType, String actionDescription,
                                 String targetType, String targetId, String ipAddress, String userAgent) {
        UserActivity activity = new UserActivity();
        activity.setUserId(userId);
        activity.setUsername(username);
        activity.setActionType(actionType);
        activity.setActionDescription(actionDescription);
        activity.setTargetType(targetType);
        activity.setTargetId(targetId);
        activity.setIpAddress(ipAddress);
        activity.setUserAgent(userAgent);
        
        return recordActivity(activity);
    }
    
    @Override
    public List<UserActivity> getRecentActivities(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认返回10条
            }
            if (limit > 100) {
                limit = 100; // 最多返回100条
            }
            
            List<UserActivity> activities = userActivityMapper.selectRecentActivities(limit);
            log.debug("获取最近活动列表成功，数量: {}", activities.size());
            return activities;
        } catch (Exception e) {
            log.error("获取最近活动列表失败: {}", e.getMessage(), e);
            return List.of(); // 返回空列表
        }
    }
    
    @Override
    public List<UserActivity> getActivitiesByUserId(Integer userId, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }
            if (limit > 100) {
                limit = 100;
            }
            
            return userActivityMapper.selectByUserId(userId, limit);
        } catch (Exception e) {
            log.error("根据用户ID获取活动列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public List<UserActivity> getActivitiesByActionType(String actionType, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }
            if (limit > 100) {
                limit = 100;
            }
            
            return userActivityMapper.selectByActionType(actionType, limit);
        } catch (Exception e) {
            log.error("根据操作类型获取活动列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public Map<String, Object> getActivityStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            int totalCount = userActivityMapper.countAll();
            int todayCount = userActivityMapper.countToday();
            
            statistics.put("totalCount", totalCount);
            statistics.put("todayCount", todayCount);
            statistics.put("lastUpdateTime", LocalDateTime.now());
            
            log.debug("活动统计信息: 总数={}, 今日={}", totalCount, todayCount);
        } catch (Exception e) {
            log.error("获取活动统计信息失败: {}", e.getMessage(), e);
            statistics.put("totalCount", 0);
            statistics.put("todayCount", 0);
            statistics.put("error", "获取统计信息失败");
        }
        
        return statistics;
    }
    
    @Override
    public int cleanOldActivities(Integer days) {
        try {
            if (days == null || days <= 0) {
                days = 30; // 默认清理30天前的记录
            }
            
            int deletedCount = userActivityMapper.deleteOldActivities(days);
            log.info("清理过期活动记录完成，删除数量: {}，保留天数: {}", deletedCount, days);
            
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期活动记录失败: {}", e.getMessage(), e);
            return 0;
        }
    }
} 