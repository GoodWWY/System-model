package com.wwy.Common;

import com.wwy.Entity.UserActivity;
import com.wwy.Service.IUserActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 活动记录工具类
 * 用于在控制器中方便地记录用户活动
 */
@Slf4j
@Component
public class ActivityLogger {
    
    @Autowired
    private IUserActivityService userActivityService;
    
    /**
     * 记录用户活动（自动获取IP和UserAgent）
     * @param userId 用户ID
     * @param username 用户名
     * @param actionType 操作类型
     * @param actionDescription 操作描述
     */
    public void log(Integer userId, String username, String actionType, String actionDescription) {
        log(userId, username, actionType, actionDescription, null, null);
    }
    
    /**
     * 记录用户活动（带目标信息）
     * @param userId 用户ID
     * @param username 用户名
     * @param actionType 操作类型
     * @param actionDescription 操作描述
     * @param targetType 目标类型
     * @param targetId 目标ID
     */
    public void log(Integer userId, String username, String actionType, String actionDescription, 
                   String targetType, String targetId) {
        try {
            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();
            
            userActivityService.recordActivity(userId, username, actionType, actionDescription, 
                    targetType, targetId, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("记录用户活动失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 记录登录活动
     * @param userId 用户ID
     * @param username 用户名
     */
    public void logLogin(Integer userId, String username) {
        log(userId, username, UserActivity.ACTION_LOGIN, "用户登录系统", 
            UserActivity.TARGET_USER, userId.toString());
    }
    
    /**
     * 记录登出活动
     * @param userId 用户ID
     * @param username 用户名
     */
    public void logLogout(Integer userId, String username) {
        log(userId, username, UserActivity.ACTION_LOGOUT, "用户退出系统", 
            UserActivity.TARGET_USER, userId.toString());
    }
    
    /**
     * 记录注册活动
     * @param userId 用户ID
     * @param username 用户名
     */
    public void logRegister(Integer userId, String username) {
        log(userId, username, UserActivity.ACTION_REGISTER, "用户注册账号", 
            UserActivity.TARGET_USER, userId.toString());
    }
    
    /**
     * 记录更新个人信息活动
     * @param userId 用户ID
     * @param username 用户名
     */
    public void logUpdateProfile(Integer userId, String username) {
        log(userId, username, UserActivity.ACTION_UPDATE_PROFILE, "修改个人信息", 
            UserActivity.TARGET_USER, userId.toString());
    }
    
    /**
     * 记录上传头像活动
     * @param userId 用户ID
     * @param username 用户名
     */
    public void logUploadAvatar(Integer userId, String username) {
        log(userId, username, UserActivity.ACTION_UPLOAD_AVATAR, "上传头像", 
            UserActivity.TARGET_USER, userId.toString());
    }
    
    /**
     * 记录创建用户活动
     * @param operatorId 操作者ID
     * @param operatorName 操作者用户名
     * @param targetUserId 目标用户ID
     * @param targetUsername 目标用户名
     */
    public void logCreateUser(Integer operatorId, String operatorName, Integer targetUserId, String targetUsername) {
        log(operatorId, operatorName, UserActivity.ACTION_CREATE_USER, 
            "创建新用户: " + targetUsername, UserActivity.TARGET_USER, targetUserId.toString());
    }
    
    /**
     * 记录修改用户活动
     * @param operatorId 操作者ID
     * @param operatorName 操作者用户名
     * @param targetUserId 目标用户ID
     * @param targetUsername 目标用户名
     */
    public void logUpdateUser(Integer operatorId, String operatorName, Integer targetUserId, String targetUsername) {
        log(operatorId, operatorName, UserActivity.ACTION_UPDATE_USER, 
            "修改用户信息: " + targetUsername, UserActivity.TARGET_USER, targetUserId.toString());
    }
    
    /**
     * 记录删除用户活动
     * @param operatorId 操作者ID
     * @param operatorName 操作者用户名
     * @param targetUserId 目标用户ID
     * @param targetUsername 目标用户名
     */
    public void logDeleteUser(Integer operatorId, String operatorName, Integer targetUserId, String targetUsername) {
        log(operatorId, operatorName, UserActivity.ACTION_DELETE_USER, 
            "删除用户: " + targetUsername, UserActivity.TARGET_USER, targetUserId.toString());
    }
    
    /**
     * 记录角色相关操作
     * @param userId 用户ID
     * @param username 用户名
     * @param actionType 操作类型
     * @param roleName 角色名称
     * @param roleId 角色ID
     */
    public void logRoleOperation(Integer userId, String username, String actionType, String roleName, String roleId) {
        String description = switch (actionType) {
            case UserActivity.ACTION_CREATE_ROLE -> "创建角色: " + roleName;
            case UserActivity.ACTION_UPDATE_ROLE -> "修改角色: " + roleName;
            case UserActivity.ACTION_DELETE_ROLE -> "删除角色: " + roleName;
            default -> "角色操作: " + roleName;
        };
        
        log(userId, username, actionType, description, UserActivity.TARGET_ROLE, roleId);
    }
    
    /**
     * 记录系统设置操作
     * @param userId 用户ID
     * @param username 用户名
     * @param description 操作描述
     */
    public void logSystemConfig(Integer userId, String username, String description) {
        log(userId, username, UserActivity.ACTION_SYSTEM_CONFIG, description, UserActivity.TARGET_SYSTEM, null);
    }
    
    /**
     * 获取客户端IP地址
     * @return IP地址
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 获取X-Forwarded-For头
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                    return xForwardedFor.split(",")[0].trim();
                }
                
                // 获取X-Real-IP头
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                    return xRealIp;
                }
                
                // 获取Proxy-Client-IP头
                String proxyClientIp = request.getHeader("Proxy-Client-IP");
                if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
                    return proxyClientIp;
                }
                
                // 获取WL-Proxy-Client-IP头
                String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
                if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
                    return wlProxyClientIp;
                }
                
                // 最后获取RemoteAddr
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("获取客户端IP地址失败: {}", e.getMessage());
        }
        
        return "unknown";
    }
    
    /**
     * 获取用户代理信息
     * @return 用户代理字符串
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userAgent = request.getHeader("User-Agent");
                return userAgent != null ? userAgent : "unknown";
            }
        } catch (Exception e) {
            log.debug("获取用户代理信息失败: {}", e.getMessage());
        }
        
        return "unknown";
    }
} 