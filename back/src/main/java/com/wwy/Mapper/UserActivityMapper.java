package com.wwy.Mapper;

import com.wwy.Entity.UserActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户活动Mapper
 */
@Mapper
public interface UserActivityMapper {
    
    /**
     * 插入用户活动记录
     * @param userActivity 用户活动对象
     * @return 影响行数
     */
    int insert(UserActivity userActivity);
    
    /**
     * 根据ID查询用户活动
     * @param id 活动ID
     * @return 用户活动对象
     */
    UserActivity selectById(@Param("id") Long id);
    
    /**
     * 查询最近的用户活动列表
     * @param limit 限制数量
     * @return 用户活动列表
     */
    List<UserActivity> selectRecentActivities(@Param("limit") Integer limit);
    
    /**
     * 根据用户ID查询活动列表
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 用户活动列表
     */
    List<UserActivity> selectByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);
    
    /**
     * 根据操作类型查询活动列表
     * @param actionType 操作类型
     * @param limit 限制数量
     * @return 用户活动列表
     */
    List<UserActivity> selectByActionType(@Param("actionType") String actionType, @Param("limit") Integer limit);
    
    /**
     * 统计总活动数量
     * @return 总数量
     */
    int countAll();
    
    /**
     * 统计今日活动数量
     * @return 今日数量
     */
    int countToday();
    
    /**
     * 删除过期的活动记录（超过指定天数）
     * @param days 保留天数
     * @return 删除数量
     */
    int deleteOldActivities(@Param("days") Integer days);
} 