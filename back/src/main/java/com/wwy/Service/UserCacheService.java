package com.wwy.Service;

import com.wwy.Common.RedisUtil;
import com.wwy.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCacheService {
    
    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String USER_PROFILE_PREFIX = "user:profile:";
    private static final String USER_STATS_PREFIX = "user:stats:";
    private static final String LOGIN_COUNT_PREFIX = "login:count:";
    private static final long CACHE_EXPIRE_TIME = 3600; // 1小时
    
    /**
     * 缓存用户基本信息
     */
    public void cacheUser(User user) {
        try {
            String key = USER_CACHE_PREFIX + user.getUid();
            redisUtil.set(key, user, CACHE_EXPIRE_TIME);
            log.info("用户信息已缓存, uid: {}", user.getUid());
        } catch (Exception e) {
            log.error("缓存用户信息失败, uid: {}", user.getUid(), e);
        }
    }
    
    /**
     * 获取缓存的用户信息
     */
    public User getCachedUser(Integer uid) {
        try {
            String key = USER_CACHE_PREFIX + uid;
            Object obj = redisUtil.get(key);
            if (obj instanceof User) {
                log.info("从缓存获取用户信息, uid: {}", uid);
                return (User) obj;
            }
        } catch (Exception e) {
            log.error("获取缓存用户信息失败, uid: {}", uid, e);
        }
        return null;
    }
    
    /**
     * 删除用户缓存
     */
    public void evictUser(Integer uid) {
        try {
            String userKey = USER_CACHE_PREFIX + uid;
            String profileKey = USER_PROFILE_PREFIX + uid;
            redisUtil.del(userKey, profileKey);
            log.info("用户缓存已清除, uid: {}", uid);
        } catch (Exception e) {
            log.error("清除用户缓存失败, uid: {}", uid, e);
        }
    }
    
    /**
     * 记录用户登录次数（计数器）
     */
    public void incrementLoginCount(Integer uid) {
        try {
            String key = LOGIN_COUNT_PREFIX + uid;
            redisUtil.incr(key, 1);
            // 设置过期时间为30天
            redisUtil.expire(key, 30 * 24 * 3600);
            log.info("用户登录次数已更新, uid: {}", uid);
        } catch (Exception e) {
            log.error("更新用户登录次数失败, uid: {}", uid, e);
        }
    }
    
    /**
     * 获取用户登录次数
     */
    public long getLoginCount(Integer uid) {
        try {
            String key = LOGIN_COUNT_PREFIX + uid;
            Object count = redisUtil.get(key);
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        } catch (Exception e) {
            log.error("获取用户登录次数失败, uid: {}", uid, e);
        }
        return 0;
    }
    
    /**
     * 缓存用户统计信息
     */
    public void cacheUserStats(String statType, Object data) {
        try {
            String key = USER_STATS_PREFIX + statType;
            redisUtil.set(key, data, CACHE_EXPIRE_TIME);
            log.info("用户统计信息已缓存, type: {}", statType);
        } catch (Exception e) {
            log.error("缓存用户统计信息失败, type: {}", statType, e);
        }
    }
    
    /**
     * 获取缓存的用户统计信息
     */
    public Object getCachedUserStats(String statType) {
        try {
            String key = USER_STATS_PREFIX + statType;
            Object data = redisUtil.get(key);
            if (data != null) {
                log.info("从缓存获取用户统计信息, type: {}", statType);
                return data;
            }
        } catch (Exception e) {
            log.error("获取缓存用户统计信息失败, type: {}", statType, e);
        }
        return null;
    }
    
    /**
     * 清除用户统计缓存
     */
    public void evictUserStats() {
        try {
            // 这里可以使用模式匹配删除所有用户统计缓存
            // 简单实现：删除常用的统计类型
            String[] statTypes = {"total", "active", "new", "growth"};
            for (String type : statTypes) {
                String key = USER_STATS_PREFIX + type;
                redisUtil.del(key);
            }
            log.info("用户统计缓存已清除");
        } catch (Exception e) {
            log.error("清除用户统计缓存失败", e);
        }
    }
    
    /**
     * 设置用户在线状态
     */
    public void setUserOnline(Integer uid) {
        try {
            String key = "user:online:" + uid;
            redisUtil.set(key, System.currentTimeMillis(), 1800); // 30分钟过期
            log.debug("用户在线状态已设置, uid: {}", uid);
        } catch (Exception e) {
            log.error("设置用户在线状态失败, uid: {}", uid, e);
        }
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Integer uid) {
        try {
            String key = "user:online:" + uid;
            return redisUtil.hasKey(key);
        } catch (Exception e) {
            log.error("检查用户在线状态失败, uid: {}", uid, e);
            return false;
        }
    }
    
    /**
     * 设置用户离线
     */
    public void setUserOffline(Integer uid) {
        try {
            String key = "user:online:" + uid;
            redisUtil.del(key);
            log.debug("用户离线状态已设置, uid: {}", uid);
        } catch (Exception e) {
            log.error("设置用户离线状态失败, uid: {}", uid, e);
        }
    }
} 