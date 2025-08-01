package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Entity.UserActivity;
import com.wwy.Service.IUserActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户活动控制器
 */
@Slf4j
@RestController
@RequestMapping("/activity")
@CrossOrigin
public class UserActivityController {
    
    @Autowired
    private IUserActivityService userActivityService;
    
    /**
     * 获取最近的用户活动列表
     * @param limit 限制数量，默认10条
     * @return 活动列表
     */
    @GetMapping("/recent")
    public Result getRecentActivities(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<UserActivity> activities = userActivityService.getRecentActivities(limit);
            
            return Result.success("获取最近活动成功", activities);
        } catch (Exception e) {
            log.error("获取最近活动失败: {}", e.getMessage(), e);
            return Result.fail("获取最近活动失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取活动列表
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 活动列表
     */
    @GetMapping("/user/{userId}")
    public Result getActivitiesByUserId(@PathVariable Integer userId, 
                                       @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<UserActivity> activities = userActivityService.getActivitiesByUserId(userId, limit);
            
            return Result.success("获取用户活动成功", activities);
        } catch (Exception e) {
            log.error("获取用户活动失败: {}", e.getMessage(), e);
            return Result.fail("获取用户活动失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据操作类型获取活动列表
     * @param actionType 操作类型
     * @param limit 限制数量
     * @return 活动列表
     */
    @GetMapping("/type/{actionType}")
    public Result getActivitiesByActionType(@PathVariable String actionType,
                                           @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<UserActivity> activities = userActivityService.getActivitiesByActionType(actionType, limit);
            
            return Result.success("获取操作类型活动成功", activities);
        } catch (Exception e) {
            log.error("获取操作类型活动失败: {}", e.getMessage(), e);
            return Result.fail("获取操作类型活动失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取活动统计信息
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result getActivityStatistics() {
        try {
            Map<String, Object> statistics = userActivityService.getActivityStatistics();
            
            return Result.success("获取活动统计成功", statistics);
        } catch (Exception e) {
            log.error("获取活动统计失败: {}", e.getMessage(), e);
            return Result.fail("获取活动统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动记录用户活动（一般用于测试或特殊情况）
     * @param userActivity 用户活动对象
     * @return 操作结果
     */
    @PostMapping("/record")
    public Result recordActivity(@RequestBody UserActivity userActivity) {
        try {
            boolean success = userActivityService.recordActivity(userActivity);
            
            if (success) {
                return Result.success("记录用户活动成功");
            } else {
                return Result.fail("记录用户活动失败");
            }
        } catch (Exception e) {
            log.error("记录用户活动失败: {}", e.getMessage(), e);
            return Result.fail("记录用户活动失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期的活动记录
     * @param days 保留天数，默认30天
     * @return 清理结果
     */
    @DeleteMapping("/clean")
    public Result cleanOldActivities(@RequestParam(defaultValue = "30") Integer days) {
        try {
            int deletedCount = userActivityService.cleanOldActivities(days);
            
            return Result.success("清理过期活动记录成功", 
                    Map.of("deletedCount", deletedCount, "retainDays", days));
        } catch (Exception e) {
            log.error("清理过期活动记录失败: {}", e.getMessage(), e);
            return Result.fail("清理过期活动记录失败: " + e.getMessage());
        }
    }
} 