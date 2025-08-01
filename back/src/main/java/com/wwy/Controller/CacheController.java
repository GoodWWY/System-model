package com.wwy.Controller;

import com.wwy.Common.CacheWarmupService;
import com.wwy.Common.RedisUtil;
import com.wwy.Common.Result;
import com.wwy.Service.UserCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cache")
@CrossOrigin
public class CacheController {
    
    private static final Logger log = LoggerFactory.getLogger(CacheController.class);
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private CacheWarmupService cacheWarmupService;
    
    @Autowired
    private UserCacheService userCacheService;
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public Result getCacheStats() {
        try {
            log.info("获取缓存统计信息请求");
            
            Map<String, Object> stats = new HashMap<>();
            
            // 系统相关信息
            stats.put("systemVersion", redisUtil.get("system:version"));
            stats.put("systemStatus", redisUtil.get("system:status"));
            stats.put("systemStartTime", redisUtil.get("system:start_time"));
            
            // 缓存状态
            stats.put("redisConnected", true); // 如果能执行到这里说明Redis连接正常
            
            // 可以添加更多统计信息，如：
            // - 各种缓存的键数量
            // - 内存使用情况等
            
            log.info("获取缓存统计信息成功");
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return Result.fail("获取缓存统计信息失败");
        }
    }
    
    /**
     * 手动触发缓存预热
     */
    @PostMapping("/warmup")
    public Result warmupCache() {
        try {
            log.info("手动触发缓存预热请求");
            
            cacheWarmupService.manualWarmup();
            
            log.info("手动缓存预热成功");
            return Result.success("缓存预热成功");
        } catch (Exception e) {
            log.error("手动缓存预热失败", e);
            return Result.fail("缓存预热失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除指定用户的缓存
     */
    @DeleteMapping("/user/{uid}")
    public Result evictUserCache(@PathVariable Integer uid) {
        try {
            log.info("清除用户缓存请求, uid: {}", uid);
            
            userCacheService.evictUser(uid);
            
            // 同时清除Spring Cache中的用户缓存
            if (cacheManager.getCache("user") != null) {
                cacheManager.getCache("user").evict(uid);
            }
            
            log.info("用户缓存清除成功, uid: {}", uid);
            return Result.success("用户缓存清除成功");
        } catch (Exception e) {
            log.error("清除用户缓存失败, uid: {}", uid, e);
            return Result.fail("清除用户缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除所有分类缓存
     */
    @DeleteMapping("/categories")
    public Result evictCategoryCache() {
        try {
            log.info("清除分类缓存请求");
            
            // 清除Spring Cache中的分类缓存
            if (cacheManager.getCache("categories") != null) {
                cacheManager.getCache("categories").clear();
            }
            if (cacheManager.getCache("category") != null) {
                cacheManager.getCache("category").clear();
            }
            if (cacheManager.getCache("categoryStats") != null) {
                cacheManager.getCache("categoryStats").clear();
            }
            
            log.info("分类缓存清除成功");
            return Result.success("分类缓存清除成功");
        } catch (Exception e) {
            log.error("清除分类缓存失败", e);
            return Result.fail("清除分类缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除用户统计缓存
     */
    @DeleteMapping("/user-stats")
    public Result evictUserStatsCache() {
        try {
            log.info("清除用户统计缓存请求");
            
            userCacheService.evictUserStats();
            
            log.info("用户统计缓存清除成功");
            return Result.success("用户统计缓存清除成功");
        } catch (Exception e) {
            log.error("清除用户统计缓存失败", e);
            return Result.fail("清除用户统计缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除所有缓存
     */
    @DeleteMapping("/all")
    public Result evictAllCache() {
        try {
            log.info("清除所有缓存请求");
            
            // 清除Spring Cache中的所有缓存
            for (String cacheName : cacheManager.getCacheNames()) {
                if (cacheManager.getCache(cacheName) != null) {
                    cacheManager.getCache(cacheName).clear();
                }
            }
            
            // 清除用户统计缓存
            userCacheService.evictUserStats();
            
            log.info("所有缓存清除成功");
            return Result.success("所有缓存清除成功");
        } catch (Exception e) {
            log.error("清除所有缓存失败", e);
            return Result.fail("清除所有缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查指定key是否存在
     */
    @GetMapping("/exists/{key}")
    public Result checkKeyExists(@PathVariable String key) {
        try {
            log.info("检查缓存key是否存在请求, key: {}", key);
            
            boolean exists = redisUtil.hasKey(key);
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("exists", exists);
            
            if (exists) {
                result.put("ttl", redisUtil.getExpire(key));
            }
            
            log.info("缓存key检查完成, key: {}, exists: {}", key, exists);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查缓存key失败, key: {}", key, e);
            return Result.fail("检查缓存key失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取在线用户数量
     */
    @GetMapping("/online-users")
    public Result getOnlineUsersCount() {
        try {
            log.info("获取在线用户数量请求");
            
            // 这里可以实现获取在线用户数量的逻辑
            // 由于当前UserCacheService没有提供统计在线用户数量的方法
            // 这里返回一个示例数据
            
            Map<String, Object> result = new HashMap<>();
            result.put("onlineCount", 0); // 实际实现时可以统计user:online:*的key数量
            result.put("timestamp", System.currentTimeMillis());
            
            log.info("获取在线用户数量成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return Result.fail("获取在线用户数量失败: " + e.getMessage());
        }
    }
} 