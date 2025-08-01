package com.wwy.Service.Impl;

import com.wwy.Entity.SystemSetting;
import com.wwy.Mapper.SystemSettingMapper;
import com.wwy.Service.ISystemSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingServiceImpl implements ISystemSettingService {
    
    private static final Logger log = LoggerFactory.getLogger(SystemSettingServiceImpl.class);
    
    @Autowired
    private SystemSettingMapper systemSettingMapper;
    
    @Override
    public List<SystemSetting> getAllSettings() {
        return systemSettingMapper.getAllSettings();
    }
    
    @Override
    public List<SystemSetting> getSettingsByType(String settingType) {
        return systemSettingMapper.getSettingsByType(settingType);
    }
    
    @Override
    public String getSettingValue(String settingKey) {
        SystemSetting setting = systemSettingMapper.getSettingByKey(settingKey);
        return setting != null ? setting.getSettingValue() : null;
    }
    
    @Override
    public String getSettingValue(String settingKey, String defaultValue) {
        String value = getSettingValue(settingKey);
        return value != null ? value : defaultValue;
    }
    
    @Override
    public SystemSetting getSettingByKey(String settingKey) {
        return systemSettingMapper.getSettingByKey(settingKey);
    }
    
    @Override
    @Transactional
    public boolean updateSetting(String settingKey, String settingValue) {
        try {
            // 先检查设置是否存在
            SystemSetting existingSetting = systemSettingMapper.getSettingByKey(settingKey);
            if (existingSetting != null) {
                // 更新现有设置
                return systemSettingMapper.updateSettingByKey(settingKey, settingValue) > 0;
            } else {
                // 创建新设置
                SystemSetting newSetting = new SystemSetting(settingKey, settingValue, "custom", "用户自定义设置");
                return systemSettingMapper.insertSetting(newSetting) > 0;
            }
        } catch (Exception e) {
            log.error("更新设置失败: " + settingKey, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchUpdateSettings(Map<String, String> settings) {
        try {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                if (!updateSetting(entry.getKey(), entry.getValue())) {
                    log.error("批量更新设置失败: " + entry.getKey());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("批量更新设置失败", e);
            return false;
        }
    }
    
    @Override
    public boolean addSetting(SystemSetting setting) {
        try {
            return systemSettingMapper.insertSetting(setting) > 0;
        } catch (Exception e) {
            log.error("添加设置失败", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteSetting(String settingKey) {
        try {
            return systemSettingMapper.deleteSettingByKey(settingKey) > 0;
        } catch (Exception e) {
            log.error("删除设置失败: " + settingKey, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean initDefaultSettings() {
        try {
            // 系统信息设置
            if (getSettingByKey("system.name") == null) {
                addSetting(new SystemSetting("system.name", "Cursor Demo 管理系统", "system", "系统名称"));
            }
            if (getSettingByKey("system.version") == null) {
                addSetting(new SystemSetting("system.version", "1.0.0", "system", "系统版本"));
            }
            if (getSettingByKey("system.description") == null) {
                addSetting(new SystemSetting("system.description", "基于Spring Boot + Vue的现代化管理系统", "system", "系统描述"));
            }
            if (getSettingByKey("system.website") == null) {
                addSetting(new SystemSetting("system.website", "http://localhost:8080", "system", "系统网址"));
            }
            
            // 文件上传设置
            if (getSettingByKey("upload.max_size") == null) {
                addSetting(new SystemSetting("upload.max_size", "10", "upload", "文件上传最大大小(MB)"));
            }
            if (getSettingByKey("upload.allowed_types") == null) {
                addSetting(new SystemSetting("upload.allowed_types", "jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx", "upload", "允许上传的文件类型"));
            }
            if (getSettingByKey("upload.avatar_max_size") == null) {
                addSetting(new SystemSetting("upload.avatar_max_size", "2", "upload", "头像上传最大大小(MB)"));
            }
            
            // 邮件配置
            if (getSettingByKey("mail.smtp_host") == null) {
                addSetting(new SystemSetting("mail.smtp_host", "smtp.qq.com", "mail", "SMTP服务器"));
            }
            if (getSettingByKey("mail.smtp_port") == null) {
                addSetting(new SystemSetting("mail.smtp_port", "587", "mail", "SMTP端口"));
            }
            if (getSettingByKey("mail.username") == null) {
                addSetting(new SystemSetting("mail.username", "", "mail", "邮箱用户名"));
            }
            if (getSettingByKey("mail.password") == null) {
                addSetting(new SystemSetting("mail.password", "", "mail", "邮箱密码"));
            }
            if (getSettingByKey("mail.from_name") == null) {
                addSetting(new SystemSetting("mail.from_name", "Cursor Demo 系统", "mail", "发件人名称"));
            }
            
            // 系统参数
            if (getSettingByKey("system.page_size") == null) {
                addSetting(new SystemSetting("system.page_size", "10", "system", "默认分页大小"));
            }
            if (getSettingByKey("system.session_timeout") == null) {
                addSetting(new SystemSetting("system.session_timeout", "30", "system", "会话超时时间(分钟)"));
            }
            if (getSettingByKey("system.maintenance_mode") == null) {
                addSetting(new SystemSetting("system.maintenance_mode", "false", "system", "维护模式"));
            }
            if (getSettingByKey("system.maintenance_message") == null) {
                addSetting(new SystemSetting("system.maintenance_message", "系统正在维护中，请稍后访问", "system", "维护提示信息"));
            }
            
            return true;
        } catch (Exception e) {
            log.error("初始化默认设置失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("name", getSettingValue("system.name", "Cursor Demo 管理系统"));
        systemInfo.put("version", getSettingValue("system.version", "1.0.0"));
        systemInfo.put("description", getSettingValue("system.description", "基于Spring Boot + Vue的现代化管理系统"));
        systemInfo.put("website", getSettingValue("system.website", "http://localhost:8080"));
        systemInfo.put("pageSize", getSettingValue("system.page_size", "10"));
        systemInfo.put("sessionTimeout", getSettingValue("system.session_timeout", "30"));
        systemInfo.put("maintenanceMode", Boolean.parseBoolean(getSettingValue("system.maintenance_mode", "false")));
        systemInfo.put("maintenanceMessage", getSettingValue("system.maintenance_message", "系统正在维护中，请稍后访问"));
        return systemInfo;
    }
    
    @Override
    public Map<String, Object> getMailConfig() {
        Map<String, Object> mailConfig = new HashMap<>();
        mailConfig.put("smtpHost", getSettingValue("mail.smtp_host", "smtp.qq.com"));
        mailConfig.put("smtpPort", getSettingValue("mail.smtp_port", "587"));
        mailConfig.put("username", getSettingValue("mail.username", ""));
        mailConfig.put("password", getSettingValue("mail.password", ""));
        mailConfig.put("fromName", getSettingValue("mail.from_name", "Cursor Demo 系统"));
        return mailConfig;
    }
    
    @Override
    public Map<String, Object> getFileUploadConfig() {
        Map<String, Object> fileConfig = new HashMap<>();
        fileConfig.put("maxSize", getSettingValue("upload.max_size", "10"));
        fileConfig.put("allowedTypes", getSettingValue("upload.allowed_types", "jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx"));
        fileConfig.put("avatarMaxSize", getSettingValue("upload.avatar_max_size", "2"));
        return fileConfig;
    }
    
    @Override
    public boolean updateSystemInfo(Map<String, String> systemInfo) {
        Map<String, String> settings = new HashMap<>();
        if (systemInfo.containsKey("name")) settings.put("system.name", systemInfo.get("name"));
        if (systemInfo.containsKey("version")) settings.put("system.version", systemInfo.get("version"));
        if (systemInfo.containsKey("description")) settings.put("system.description", systemInfo.get("description"));
        if (systemInfo.containsKey("website")) settings.put("system.website", systemInfo.get("website"));
        if (systemInfo.containsKey("pageSize")) settings.put("system.page_size", systemInfo.get("pageSize"));
        if (systemInfo.containsKey("sessionTimeout")) settings.put("system.session_timeout", systemInfo.get("sessionTimeout"));
        if (systemInfo.containsKey("maintenanceMode")) settings.put("system.maintenance_mode", systemInfo.get("maintenanceMode"));
        if (systemInfo.containsKey("maintenanceMessage")) settings.put("system.maintenance_message", systemInfo.get("maintenanceMessage"));
        
        return batchUpdateSettings(settings);
    }
    
    @Override
    public boolean updateMailConfig(Map<String, String> mailConfig) {
        Map<String, String> settings = new HashMap<>();
        if (mailConfig.containsKey("smtpHost")) settings.put("mail.smtp_host", mailConfig.get("smtpHost"));
        if (mailConfig.containsKey("smtpPort")) settings.put("mail.smtp_port", mailConfig.get("smtpPort"));
        if (mailConfig.containsKey("username")) settings.put("mail.username", mailConfig.get("username"));
        if (mailConfig.containsKey("password")) settings.put("mail.password", mailConfig.get("password"));
        if (mailConfig.containsKey("fromName")) settings.put("mail.from_name", mailConfig.get("fromName"));
        
        return batchUpdateSettings(settings);
    }
    
    @Override
    public boolean updateFileUploadConfig(Map<String, String> fileConfig) {
        Map<String, String> settings = new HashMap<>();
        if (fileConfig.containsKey("maxSize")) settings.put("upload.max_size", fileConfig.get("maxSize"));
        if (fileConfig.containsKey("allowedTypes")) settings.put("upload.allowed_types", fileConfig.get("allowedTypes"));
        if (fileConfig.containsKey("avatarMaxSize")) settings.put("upload.avatar_max_size", fileConfig.get("avatarMaxSize"));
        
        return batchUpdateSettings(settings);
    }
} 