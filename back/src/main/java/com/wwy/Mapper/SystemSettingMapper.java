package com.wwy.Mapper;

import com.wwy.Entity.SystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemSettingMapper {
    
    /**
     * 获取所有设置
     */
    List<SystemSetting> getAllSettings();
    
    /**
     * 根据设置键获取设置
     */
    SystemSetting getSettingByKey(@Param("settingKey") String settingKey);
    
    /**
     * 根据设置类型获取设置列表
     */
    List<SystemSetting> getSettingsByType(@Param("settingType") String settingType);
    
    /**
     * 插入设置
     */
    int insertSetting(SystemSetting setting);
    
    /**
     * 更新设置
     */
    int updateSetting(SystemSetting setting);
    
    /**
     * 根据键更新设置值
     */
    int updateSettingByKey(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue);
    
    /**
     * 删除设置
     */
    int deleteSetting(@Param("id") Integer id);
    
    /**
     * 根据键删除设置
     */
    int deleteSettingByKey(@Param("settingKey") String settingKey);
    
    /**
     * 批量插入或更新设置
     */
    int batchUpsertSettings(@Param("settings") List<SystemSetting> settings);
} 