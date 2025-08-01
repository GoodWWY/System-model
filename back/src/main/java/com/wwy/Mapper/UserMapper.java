package com.wwy.Mapper;

import com.wwy.Entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);
    
    /**
     * 统计指定邮箱的用户数量
     */
    int countByEmail(String email);
    
    void insertUser(User user);
    List<User> listUsers(@Param("offset") int offset, @Param("pageSize") int pageSize);
    int countUsers();
    User getUserById(Integer uid);
    int updateUser(User user);
    
    /**
     * 删除用户
     */
    int deleteUser(@Param("uid") Integer uid);
    
    // 添加根据多条件搜索用户的方法（旧版本，保留兼容性）
    List<User> searchUsers(@Param("keyword") String keyword, 
                          @Param("status") String status, 
                          @Param("gender") String gender, 
                          @Param("roleId") Integer roleId,
                          @Param("offset") int offset, 
                          @Param("pageSize") int pageSize);
    
    // 获取搜索结果的总数（旧版本）
    int countSearchUsers(@Param("keyword") String keyword, 
                        @Param("status") String status, 
                        @Param("gender") String gender,
                        @Param("roleId") Integer roleId);
    
    /**
     * 新的搜索用户方法，支持用户名、邮箱等精确搜索
     */
    List<User> searchUsersWithRoles(@Param("username") String username,
                                   @Param("email") String email,
                                   @Param("status") String status,
                                   @Param("roleId") Integer roleId,
                                   @Param("offset") int offset,
                                   @Param("pageSize") int pageSize);
    
    /**
     * 统计新搜索方法的结果数量
     */
    int countSearchUsersWithRoles(@Param("username") String username,
                                 @Param("email") String email,
                                 @Param("status") String status,
                                 @Param("roleId") Integer roleId);
                        
    // 批量删除用户
    int batchDeleteUsers(@Param("uids") List<Integer> uids);
    
    // 查询拥有指定角色的所有用户
    List<User> getUsersByRoleId(@Param("roleId") Integer roleId, 
                              @Param("offset") int offset, 
                              @Param("pageSize") int pageSize);
    
    // 统计拥有指定角色的用户数量
    int countUsersByRoleId(@Param("roleId") Integer roleId);
    
    // 获取用户及其角色信息
    User getUserWithRoles(@Param("uid") Integer uid);
    
    // 查询所有用户及其角色信息
    List<User> listUsersWithRoles(@Param("offset") int offset, @Param("pageSize") int pageSize);
}
