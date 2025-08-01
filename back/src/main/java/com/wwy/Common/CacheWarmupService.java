package com.wwy.Common;

import com.wwy.Service.ICategoryService;
import com.wwy.Service.UserCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 缓存预热服务
 * 在系统启动时预加载常用数据到Redis缓存中
 */
@Component
public class CacheWarmupService implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(CacheWarmupService.class);
    
    @Autowired
    private ICategoryService categoryService;
    
    @Autowired
    private UserCacheService userCacheService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始执行缓存预热...");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 预热分类数据
            warmupCategoryData();
            
            // 预热系统统计数据
            warmupSystemStats();
            
            // 设置系统启动时间
            setSystemStartTime();
            
            long endTime = System.currentTimeMillis();
            log.info("缓存预热完成，耗时: {}ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }
    
    /**
     * 预热分类数据
     */
    private void warmupCategoryData() {
        try {
            log.info("开始预热分类数据");
            
            // 预加载所有分类
            categoryService.getAllCategories();
            
            // 预加载启用的分类
            categoryService.getEnabledCategories();
            
            // 预加载分类统计信息
            categoryService.getCategoryStatistics();
            
            log.info("分类数据预热完成");
            
        } catch (Exception e) {
            log.error("分类数据预热失败", e);
        }
    }
    
    /**
     * 预热系统统计数据
     */
    private void warmupSystemStats() {
        try {
            log.info("开始预热系统统计数据");
            
            // 这里可以预加载一些系统级别的统计数据
            // 例如：总用户数、活跃用户数等
            // 由于这些数据可能需要复杂的查询，先设置一些基础数据
            
            // 设置系统基本信息
            redisUtil.set("system:version", "1.0.0", 24 * 3600); // 24小时过期
            redisUtil.set("system:status", "running", 60); // 1分钟过期，用于健康检查
            
            log.info("系统统计数据预热完成");
            
        } catch (Exception e) {
            log.error("系统统计数据预热失败", e);
        }
    }
    
    /**
     * 设置系统启动时间
     */
    private void setSystemStartTime() {
        try {
            long startTime = System.currentTimeMillis();
            redisUtil.set("system:start_time", startTime);
            log.info("系统启动时间已设置: {}", startTime);
        } catch (Exception e) {
            log.error("设置系统启动时间失败", e);
        }
    }
    
    /**
     * 手动触发缓存预热（可用于运维）
     */
    public void manualWarmup() {
        log.info("手动触发缓存预热");
        try {
            run(null);
        } catch (Exception e) {
            log.error("手动缓存预热失败", e);
        }
    }
} 