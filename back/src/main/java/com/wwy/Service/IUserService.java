package com.wwy.Service;

import com.wwy.Entity.User;
import java.util.List;
import java.util.Map;

public interface IUserService {
    User login(String username, String password);
    
    // 修改注册方法，添加邮箱参数
    boolean register(String username, String password, String email);
    
    // 检查邮箱是否已存在
    boolean isEmailExists(String email);
    
    // 通过邮箱获取用户
    User getUserByEmail(String email);
    
    List<User> listUsers(int pageNum, int pageSize);
    int countUsers();
    User getUserById(Integer uid);
    boolean updateUser(User user);
    boolean updateUserProfile(Integer uid, User user);
    boolean deleteUser(Integer uid);
    
    // 修改搜索用户方法参数
    List<User> searchUsersWithRoles(Map<String, Object> params);
    int countSearchUsers(Map<String, Object> params);
    
    // 批量删除用户
    boolean batchDeleteUsers(List<Integer> uids);
    
    // 通过用户名获取用户
    User getUserByUsername(String username);
    
    // 添加用户
    boolean addUser(User user);
    
    // 添加角色相关方法
    
    // 获取用户及其角色信息
    User getUserWithRoles(Integer uid);
    
    // 查询所有用户及其角色信息
    List<User> listUsersWithRoles(int pageNum, int pageSize);
    
    // 为用户分配角色
    boolean assignRoleToUser(Integer userId, Integer roleId);
    
    // 为用户分配多个角色
    boolean assignRolesToUser(Integer userId, List<Integer> roleIds);
    
    // 移除用户的角色
    boolean removeRoleFromUser(Integer userId, Integer roleId);
    
    // 移除用户的所有角色
    boolean removeAllRolesFromUser(Integer userId);
    
    // 检查用户是否拥有指定角色
    boolean checkUserHasRole(Integer userId, Integer roleId);
    
    // 检查用户是否拥有指定角色代码
    boolean checkUserHasRoleCode(Integer userId, String roleCode);
    
    // 检查用户是否是管理员
    boolean isAdmin(Integer userId);
    
    // 搜索用户方法
    Map<String, Object> searchUsers(String keyword, String status, String gender, Integer roleId, int pageNum, int pageSize);
    
    // 导出用户数据到Excel，修改参数
    byte[] exportUsersToExcel(Map<String, Object> params) throws Exception;
}
