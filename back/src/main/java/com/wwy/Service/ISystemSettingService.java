package com.wwy.Service;

import com.wwy.Entity.SystemSetting;

import java.util.List;
import java.util.Map;

public interface ISystemSettingService {
    
    /**
     * 获取所有设置
     */
    List<SystemSetting> getAllSettings();
    
    /**
     * 根据设置类型获取设置
     */
    List<SystemSetting> getSettingsByType(String settingType);
    
    /**
     * 根据设置键获取设置值
     */
    String getSettingValue(String settingKey);
    
    /**
     * 根据设置键获取设置值，如果不存在返回默认值
     */
    String getSettingValue(String settingKey, String defaultValue);
    
    /**
     * 根据设置键获取设置对象
     */
    SystemSetting getSettingByKey(String settingKey);
    
    /**
     * 更新单个设置
     */
    boolean updateSetting(String settingKey, String settingValue);
    
    /**
     * 批量更新设置
     */
    boolean batchUpdateSettings(Map<String, String> settings);
    
    /**
     * 添加新设置
     */
    boolean addSetting(SystemSetting setting);
    
    /**
     * 删除设置
     */
    boolean deleteSetting(String settingKey);
    
    /**
     * 初始化默认设置
     */
    boolean initDefaultSettings();
    
    /**
     * 获取系统信息设置
     */
    Map<String, Object> getSystemInfo();
    
    /**
     * 获取邮件配置设置
     */
    Map<String, Object> getMailConfig();
    
    /**
     * 获取文件上传配置
     */
    Map<String, Object> getFileUploadConfig();
    
    /**
     * 更新系统信息设置
     */
    boolean updateSystemInfo(Map<String, String> systemInfo);
    
    /**
     * 更新邮件配置
     */
    boolean updateMailConfig(Map<String, String> mailConfig);
    
    /**
     * 更新文件上传配置
     */
    boolean updateFileUploadConfig(Map<String, String> fileConfig);
} 