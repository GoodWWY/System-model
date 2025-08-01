package com.wwy.Service.Impl;

import com.wwy.Common.BCryptUtil;
import com.wwy.Entity.Role;
import com.wwy.Entity.User;
import com.wwy.Mapper.UserMapper;
import com.wwy.Service.IUserService;
import com.wwy.Service.IRoleService;
import com.wwy.Service.UserCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Apache POI 导入
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class IUserServiceImpl implements IUserService {
    
    private static final Logger log = LoggerFactory.getLogger(IUserServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private IRoleService roleService;
    
    @Autowired
    private UserCacheService userCacheService;
    
    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && BCryptUtil.matches(password, user.getPassword())) {
            // 缓存用户信息
            userCacheService.cacheUser(user);
            // 记录登录次数
            userCacheService.incrementLoginCount(user.getUid());
            // 设置用户在线状态
            userCacheService.setUserOnline(user.getUid());
            
            log.info("用户登录成功，已缓存用户信息, username: {}, uid: {}", username, user.getUid());
            return user;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean register(String username, String password, String email) {
        // 检查用户名是否已存在
        User existByUsername = userMapper.findByUsername(username);
        if (existByUsername != null) return false;
        
        // 检查邮箱是否已存在
        User existByEmail = userMapper.findByEmail(email);
        if (existByEmail != null) return false;
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCryptUtil.encode(password)); // 使用BCrypt加密
        user.setEmail(email);
        user.setStatus("1"); // 设置默认状态为正常
        userMapper.insertUser(user);
        
        log.info("用户注册成功: username={}, email={}", username, email);
        return true;
    }

    @Override
    public boolean isEmailExists(String email) {
        int count = userMapper.countByEmail(email);
        return count > 0;
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public List<User> listUsers(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return userMapper.listUsers(offset, pageSize);
    }
    
    @Override
    public int countUsers() {
        return userMapper.countUsers();
    }
    
    @Override
    @Cacheable(value = "user", key = "#uid")
    public User getUserById(Integer uid) {
        // 先尝试从Redis缓存获取
        User cachedUser = userCacheService.getCachedUser(uid);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // 缓存未命中，从数据库查询
        User user = userMapper.getUserById(uid);
        if (user != null) {
            // 缓存查询结果
            userCacheService.cacheUser(user);
            log.info("从数据库查询用户并缓存, uid: {}", uid);
        }
        return user;
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#user.uid")
    public boolean updateUser(User user) {
        if (user.getUid() == null) {
            return false;
        }
        
        // 如果需要更新密码，进行加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(BCryptUtil.encode(user.getPassword()));
        }
        
        boolean result = userMapper.updateUser(user) > 0;
        if (result) {
            // 清除Redis缓存
            userCacheService.evictUser(user.getUid());
            log.info("用户信息已更新，缓存已清除, uid: {}", user.getUid());
        }
        return result;
    }
    
    @Override
    @Transactional
    public boolean updateUserProfile(Integer uid, User user) {
        if (uid == null) {
            return false;
        }
        
        // 设置用户ID，确保更新的是正确的用户
        user.setUid(uid);
        
        // 不允许通过这个方法更新密码，确保安全
        user.setPassword(null);
        
        // 处理空字符串问题 - 确保即使是空字符串也会被保存到数据库
        // 注意：这里不再将空字符串替换为空格，而是确保它们不为null
        if (user.getAddress() == null) {
            user.setAddress("");
        }
        
        if (user.getBio() == null) {
            user.setBio("");
        }
        
        return userMapper.updateUser(user) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#uid")
    public boolean deleteUser(Integer uid) {
        // 清除用户缓存
        userCacheService.evictUser(uid);
        
        return userMapper.deleteUser(uid) > 0;
    }

    @Override
    public List<User> searchUsersWithRoles(Map<String, Object> params) {
        // 获取分页参数
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        int offset = (pageNum - 1) * pageSize;
        
        // 构建查询参数
        String username = (String) params.get("username");
        String email = (String) params.get("email");
        String status = (String) params.get("status");
        Integer roleId = (Integer) params.get("roleId");
        
        return userMapper.searchUsersWithRoles(username, email, status, roleId, offset, pageSize);
    }

    @Override
    public int countSearchUsers(Map<String, Object> params) {
        String username = (String) params.get("username");
        String email = (String) params.get("email");
        String status = (String) params.get("status");
        Integer roleId = (Integer) params.get("roleId");
        
        return userMapper.countSearchUsersWithRoles(username, email, status, roleId);
    }
    
    @Override
    public Map<String, Object> searchUsers(String keyword, String status, String gender, Integer roleId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        
        // 转换状态值
        String statusValue = null;
        if (status != null && !status.isEmpty()) {
            statusValue = "1".equals(status) ? "active" : "inactive";
        }
        
        // 执行搜索
        List<User> users = userMapper.searchUsers(keyword, statusValue, gender, roleId, offset, pageSize);
        int total = userMapper.countSearchUsers(keyword, statusValue, gender, roleId);
        
        // 处理返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", users);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return result;
    }

    @Override
    @Transactional
    public boolean batchDeleteUsers(List<Integer> uids) {
        if (uids == null || uids.isEmpty()) {
            return false;
        }
        
        return userMapper.batchDeleteUsers(uids) > 0;
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    @Transactional
    public boolean addUser(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }
        
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            log.error("用户名已存在: {}", user.getUsername());
            return false;
        }
        
        // 如果密码为空，设置默认密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        
        // 加密密码
        String encryptedPassword = BCryptUtil.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // 如果性别为空，设置为保密
        if (user.getGender() == null) {
            user.setGender("保密");
        }
        
        // 处理状态字段
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        else if (!"active".equals(user.getStatus()) && !"inactive".equals(user.getStatus())) {
            boolean isActive = "true".equalsIgnoreCase(user.getStatus()) || "1".equals(user.getStatus());
            user.setStatus(isActive ? "active" : "inactive");
        }
        
        // 插入用户
        try {
            userMapper.insertUser(user);
            
            // 如果指定了角色，为用户分配角色
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                List<Integer> roleIds = user.getRoles().stream()
                        .map(Role::getId)
                        .collect(Collectors.toList());
                roleService.assignRolesToUser(user.getUid(), roleIds);
            } else {
                // 默认分配普通用户角色
                Role userRole = roleService.getRoleByCode("ROLE_USER");
                if (userRole != null) {
                    roleService.assignRoleToUser(user.getUid(), userRole.getId());
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return false;
        }
    }
    
    @Override
    public User getUserWithRoles(Integer uid) {
        User user = userMapper.getUserWithRoles(uid);
        if (user != null && user.getRoles() != null && !user.getRoles().isEmpty()) {
            // 设置角色名称，用于前端显示
            StringBuilder roleNames = new StringBuilder();
            for (int i = 0; i < user.getRoles().size(); i++) {
                if (i > 0) {
                    roleNames.append(", ");
                }
                roleNames.append(user.getRoles().get(i).getName());
            }
            user.setRoleName(roleNames.toString());
        }
        return user;
    }
    
    @Override
    public List<User> listUsersWithRoles(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return userMapper.listUsersWithRoles(offset, pageSize);
    }
    
    @Override
    public boolean assignRoleToUser(Integer userId, Integer roleId) {
        return roleService.assignRoleToUser(userId, roleId);
    }
    
    @Override
    public boolean assignRolesToUser(Integer userId, List<Integer> roleIds) {
        return roleService.assignRolesToUser(userId, roleIds);
    }
    
    @Override
    public boolean removeRoleFromUser(Integer userId, Integer roleId) {
        return roleService.removeRoleFromUser(userId, roleId);
    }
    
    @Override
    public boolean removeAllRolesFromUser(Integer userId) {
        return roleService.removeAllRolesFromUser(userId);
    }
    
    @Override
    public boolean checkUserHasRole(Integer userId, Integer roleId) {
        return roleService.checkUserHasRole(userId, roleId);
    }
    
    @Override
    public boolean checkUserHasRoleCode(Integer userId, String roleCode) {
        return roleService.checkUserHasRoleCode(userId, roleCode);
    }
    
    @Override
    public boolean isAdmin(Integer userId) {
        return roleService.checkUserHasRoleCode(userId, "ROLE_ADMIN");
    }
    
    @Override
    public byte[] exportUsersToExcel(Map<String, Object> params) throws Exception {
        String username = (String) params.get("username");
        String email = (String) params.get("email");
        String status = (String) params.get("status");
        Integer roleId = (Integer) params.get("roleId");
        log.info("开始导出用户数据，参数：username={}, email={}, status={}, roleId={}", username, email, status, roleId);
        
        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("用户列表");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户ID", "用户名", "性别", "邮箱", "手机号", "年龄", "地址", "个人简介", "状态", "角色"};
            
            // 设置标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // 填充标题
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 获取用户数据 - 不分页，获取所有数据
            Map<String, Object> exportParams = new HashMap<>();
            exportParams.put("username", username);
            exportParams.put("email", email);
            exportParams.put("status", status);
            exportParams.put("roleId", roleId);
            exportParams.put("pageNum", 1);
            exportParams.put("pageSize", 10000);
            
            List<User> users = searchUsersWithRoles(exportParams);
            
            log.info("获取到用户数据，共 {} 条", users != null ? users.size() : 0);
            
            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // 填充数据
            int rowNum = 1;
            
            if (users != null && !users.isEmpty()) {
                for (User user : users) {
                    try {
                        Row row = sheet.createRow(rowNum++);
                        
                        // 用户ID
                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(user.getUid() != null ? user.getUid() : 0);
                        cell0.setCellStyle(dataStyle);
                        
                        // 用户名
                        Cell cell1 = row.createCell(1);
                        cell1.setCellValue(user.getUsername() != null ? user.getUsername() : "");
                        cell1.setCellStyle(dataStyle);
                        
                        // 性别
                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(user.getGender() != null ? user.getGender() : "保密");
                        cell2.setCellStyle(dataStyle);
                        
                        // 邮箱
                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue(user.getEmail() != null ? user.getEmail() : "");
                        cell3.setCellStyle(dataStyle);
                        
                        // 手机号
                        Cell cell4 = row.createCell(4);
                        cell4.setCellValue(user.getPhone() != null ? user.getPhone() : "");
                        cell4.setCellStyle(dataStyle);
                        
                        // 年龄
                        Cell cell5 = row.createCell(5);
                        cell5.setCellValue(user.getAge() != null ? user.getAge() : 0);
                        cell5.setCellStyle(dataStyle);
                        
                        // 地址
                        Cell cell6 = row.createCell(6);
                        cell6.setCellValue(user.getAddress() != null ? user.getAddress() : "");
                        cell6.setCellStyle(dataStyle);
                        
                        // 个人简介
                        Cell cell7 = row.createCell(7);
                        cell7.setCellValue(user.getBio() != null ? user.getBio() : "");
                        cell7.setCellStyle(dataStyle);
                        
                        // 状态
                        Cell cell8 = row.createCell(8);
                        String statusText = "未知";
                        if (user.getStatus() != null) {
                            if ("1".equals(user.getStatus()) || "true".equals(user.getStatus().toLowerCase())) {
                                statusText = "正常";
                            } else if ("0".equals(user.getStatus()) || "false".equals(user.getStatus().toLowerCase())) {
                                statusText = "禁用";
                            } else {
                                statusText = user.getStatus();
                            }
                        }
                        cell8.setCellValue(statusText);
                        cell8.setCellStyle(dataStyle);
                        
                        // 角色
                        Cell cell9 = row.createCell(9);
                        String roleText = "";
                        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                            StringBuilder roleNames = new StringBuilder();
                            for (Role role : user.getRoles()) {
                                if (roleNames.length() > 0) {
                                    roleNames.append(", ");
                                }
                                roleNames.append(role.getName() != null ? role.getName() : "");
                            }
                            roleText = roleNames.toString();
                        } else if (user.getRoleName() != null && !user.getRoleName().trim().isEmpty()) {
                            roleText = user.getRoleName();
                        }
                        cell9.setCellValue(roleText);
                        cell9.setCellStyle(dataStyle);
                        
                    } catch (Exception e) {
                        log.error("处理用户数据时出错，用户ID: {}, 错误: {}", user.getUid(), e.getMessage());
                        // 跳过这条记录，继续处理下一条
                        continue;
                    }
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                    // 设置最小列宽和最大列宽
                    int columnWidth = sheet.getColumnWidth(i);
                    if (columnWidth < 2000) {
                        sheet.setColumnWidth(i, 2000);
                    }
                    if (columnWidth > 8000) {
                        sheet.setColumnWidth(i, 8000);
                    }
                } catch (Exception e) {
                    log.warn("调整列宽失败，列索引: {}", i);
                }
            }
            
            // 冻结标题行
            sheet.createFreezePane(0, 1);
            
            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] result = outputStream.toByteArray();
            outputStream.close();
            
            log.info("用户数据导出完成，共导出 {} 条记录", users != null ? users.size() : 0);
            return result;
            
        } catch (IOException e) {
            log.error("导出用户数据时发生IO异常", e);
            throw new Exception("导出用户数据失败：IO错误 - " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("导出用户数据时发生异常", e);
            throw new Exception("导出用户数据失败：" + e.getMessage(), e);
        }
    }
}
