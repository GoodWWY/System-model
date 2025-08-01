package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Entity.SystemSetting;
import com.wwy.Service.ISystemSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/settings")
public class SystemSettingController {
    
    private static final Logger log = LoggerFactory.getLogger(SystemSettingController.class);
    
    @Autowired
    private ISystemSettingService systemSettingService;
    
    /**
     * 获取所有设置
     */
    @GetMapping("/all")
    public Result getAllSettings() {
        try {
            List<SystemSetting> settings = systemSettingService.getAllSettings();
            return Result.success(settings);
        } catch (Exception e) {
            log.error("获取所有设置失败", e);
            return Result.fail("获取设置失败");
        }
    }
    
    /**
     * 根据类型获取设置
     */
    @GetMapping("/type/{settingType}")
    public Result getSettingsByType(@PathVariable String settingType) {
        try {
            List<SystemSetting> settings = systemSettingService.getSettingsByType(settingType);
            return Result.success(settings);
        } catch (Exception e) {
            log.error("根据类型获取设置失败: " + settingType, e);
            return Result.fail("获取设置失败");
        }
    }
    
    /**
     * 获取单个设置值
     */
    @GetMapping("/value/{settingKey}")
    public Result getSettingValue(@PathVariable String settingKey) {
        try {
            String value = systemSettingService.getSettingValue(settingKey);
            if (value != null) {
                return Result.success(value);
            } else {
                return Result.fail("设置不存在");
            }
        } catch (Exception e) {
            log.error("获取设置值失败: " + settingKey, e);
            return Result.fail("获取设置值失败");
        }
    }
    
    /**
     * 获取系统信息设置
     */
    @GetMapping("/system-info")
    public Result getSystemInfo() {
        try {
            Map<String, Object> systemInfo = systemSettingService.getSystemInfo();
            return Result.success(systemInfo);
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            return Result.fail("获取系统信息失败");
        }
    }
    
    /**
     * 获取邮件配置
     */
    @GetMapping("/mail-config")
    public Result getMailConfig() {
        try {
            Map<String, Object> mailConfig = systemSettingService.getMailConfig();
            return Result.success(mailConfig);
        } catch (Exception e) {
            log.error("获取邮件配置失败", e);
            return Result.fail("获取邮件配置失败");
        }
    }
    
    /**
     * 获取文件上传配置
     */
    @GetMapping("/file-upload-config")
    public Result getFileUploadConfig() {
        try {
            Map<String, Object> fileConfig = systemSettingService.getFileUploadConfig();
            return Result.success(fileConfig);
        } catch (Exception e) {
            log.error("获取文件上传配置失败", e);
            return Result.fail("获取文件上传配置失败");
        }
    }
    
    /**
     * 更新单个设置
     */
    @PutMapping("/update")
    public Result updateSetting(@RequestParam String settingKey, @RequestParam String settingValue) {
        try {
            boolean success = systemSettingService.updateSetting(settingKey, settingValue);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.fail("更新失败");
            }
        } catch (Exception e) {
            log.error("更新设置失败: " + settingKey, e);
            return Result.fail("更新设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新设置
     */
    @PutMapping("/batch-update")
    public Result batchUpdateSettings(@RequestBody Map<String, String> settings) {
        try {
            boolean success = systemSettingService.batchUpdateSettings(settings);
            if (success) {
                return Result.success("批量更新成功");
            } else {
                return Result.fail("批量更新失败");
            }
        } catch (Exception e) {
            log.error("批量更新设置失败", e);
            return Result.fail("批量更新设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新系统信息
     */
    @PutMapping("/system-info")
    public Result updateSystemInfo(@RequestBody Map<String, String> systemInfo) {
        try {
            boolean success = systemSettingService.updateSystemInfo(systemInfo);
            if (success) {
                return Result.success("系统信息更新成功");
            } else {
                return Result.fail("系统信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新系统信息失败", e);
            return Result.fail("更新系统信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新邮件配置
     */
    @PutMapping("/mail-config")
    public Result updateMailConfig(@RequestBody Map<String, String> mailConfig) {
        try {
            boolean success = systemSettingService.updateMailConfig(mailConfig);
            if (success) {
                return Result.success("邮件配置更新成功");
            } else {
                return Result.fail("邮件配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新邮件配置失败", e);
            return Result.fail("更新邮件配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新文件上传配置
     */
    @PutMapping("/file-upload-config")
    public Result updateFileUploadConfig(@RequestBody Map<String, String> fileConfig) {
        try {
            boolean success = systemSettingService.updateFileUploadConfig(fileConfig);
            if (success) {
                return Result.success("文件上传配置更新成功");
            } else {
                return Result.fail("文件上传配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新文件上传配置失败", e);
            return Result.fail("更新文件上传配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化默认设置
     */
    @PostMapping("/init")
    public Result initDefaultSettings() {
        try {
            boolean success = systemSettingService.initDefaultSettings();
            if (success) {
                return Result.success("默认设置初始化成功");
            } else {
                return Result.fail("默认设置初始化失败");
            }
        } catch (Exception e) {
            log.error("初始化默认设置失败", e);
            return Result.fail("初始化默认设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新设置
     */
    @PostMapping("/add")
    public Result addSetting(@RequestBody SystemSetting setting) {
        try {
            boolean success = systemSettingService.addSetting(setting);
            if (success) {
                return Result.success("添加设置成功");
            } else {
                return Result.fail("添加设置失败");
            }
        } catch (Exception e) {
            log.error("添加设置失败", e);
            return Result.fail("添加设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除设置
     */
    @DeleteMapping("/delete/{settingKey}")
    public Result deleteSetting(@PathVariable String settingKey) {
        try {
            boolean success = systemSettingService.deleteSetting(settingKey);
            if (success) {
                return Result.success("删除设置成功");
            } else {
                return Result.fail("删除设置失败");
            }
        } catch (Exception e) {
            log.error("删除设置失败: " + settingKey, e);
            return Result.fail("删除设置失败: " + e.getMessage());
        }
    }
} 