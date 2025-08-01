package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Entity.Role;
import com.wwy.Entity.User;
import com.wwy.Service.IRoleService;
import com.wwy.Service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/role")
public class RoleController {
    
    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    
    @Autowired
    private IRoleService roleService;
    
    @Autowired
    private IUserService userService;
    
    /**
     * 获取所有角色
     */
    @GetMapping("/list")
    public Result getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.fail("获取角色列表失败");
        }
    }
    
    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    public Result getRoleById(@PathVariable Integer id) {
        try {
            Role role = roleService.getRoleById(id);
            if (role == null) {
                return Result.fail("角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            log.error("获取角色失败", e);
            return Result.fail("获取角色失败");
        }
    }
    
    /**
     * 添加角色
     */
    @PostMapping("/add")
    public Result addRole(@RequestBody Role role) {
        try {
            // 检查角色名和代码是否已存在
            Role existingRole = roleService.getRoleByName(role.getName());
            if (existingRole != null) {
                return Result.fail("角色名已存在");
            }
            
            existingRole = roleService.getRoleByCode(role.getCode());
            if (existingRole != null) {
                return Result.fail("角色代码已存在");
            }
            
            boolean success = roleService.addRole(role);
            if (success) {
                return Result.success(role);
            } else {
                return Result.fail("添加角色失败");
            }
        } catch (Exception e) {
            log.error("添加角色失败", e);
            return Result.fail("添加角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/update")
    public Result updateRole(@RequestBody Role role) {
        try {
            if (role.getId() == null) {
                return Result.fail("角色ID不能为空");
            }
            
            // 检查角色是否存在
            Role existingRole = roleService.getRoleById(role.getId());
            if (existingRole == null) {
                return Result.fail("角色不存在");
            }
            
            // 检查角色名是否已被其他角色使用
            if (role.getName() != null && !role.getName().equals(existingRole.getName())) {
                Role roleWithSameName = roleService.getRoleByName(role.getName());
                if (roleWithSameName != null && !roleWithSameName.getId().equals(role.getId())) {
                    return Result.fail("角色名已被其他角色使用");
                }
            }
            
            // 检查角色代码是否已被其他角色使用
            if (role.getCode() != null && !role.getCode().equals(existingRole.getCode())) {
                Role roleWithSameCode = roleService.getRoleByCode(role.getCode());
                if (roleWithSameCode != null && !roleWithSameCode.getId().equals(role.getId())) {
                    return Result.fail("角色代码已被其他角色使用");
                }
            }
            
            boolean success = roleService.updateRole(role);
            if (success) {
                return Result.success(role);
            } else {
                return Result.fail("更新角色失败");
            }
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.fail("更新角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteRole(@PathVariable Integer id) {
        try {
            // 检查角色是否存在
            Role role = roleService.getRoleById(id);
            if (role == null) {
                return Result.fail("角色不存在");
            }
            
            // 如果是管理员角色，不允许删除
            if ("ROLE_ADMIN".equals(role.getCode())) {
                return Result.fail("管理员角色不能删除");
            }
            
            boolean success = roleService.deleteRole(id);
            if (success) {
                return Result.success("删除角色成功");
            } else {
                return Result.fail("删除角色失败");
            }
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.fail("删除角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的角色
     */
    @GetMapping("/user/{userId}")
    public Result getUserRoles(@PathVariable Integer userId) {
        try {
            // 检查用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            
            List<Role> roles = roleService.getRolesByUserId(userId);
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取用户角色失败", e);
            return Result.fail("获取用户角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/assign")
    public Result assignRolesToUser(@RequestParam Integer userId, @RequestBody List<Integer> roleIds) {
        try {
            // 检查用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            
            boolean success = userService.assignRolesToUser(userId, roleIds);
            if (success) {
                return Result.success("分配角色成功");
            } else {
                return Result.fail("分配角色失败");
            }
        } catch (Exception e) {
            log.error("分配角色失败", e);
            return Result.fail("分配角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否是管理员
     */
    @GetMapping("/check-admin/{userId}")
    public Result checkUserIsAdmin(@PathVariable Integer userId) {
        try {
            // 检查用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            
            boolean isAdmin = userService.isAdmin(userId);
            Map<String, Boolean> result = new HashMap<>();
            result.put("isAdmin", isAdmin);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查用户是否是管理员失败", e);
            return Result.fail("检查用户是否是管理员失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化角色数据
     */
    @PostMapping("/init")
    public Result initRoles() {
        try {
            // 检查角色是否已初始化
            List<Role> existingRoles = roleService.getAllRoles();
            if (!existingRoles.isEmpty()) {
                return Result.fail("角色已初始化，不需要重复初始化");
            }
            
            // 创建管理员角色
            Role adminRole = new Role();
            adminRole.setName("管理员");
            adminRole.setDescription("系统管理员，拥有所有权限");
            adminRole.setCode("ROLE_ADMIN");
            adminRole.setStatus("active");
            roleService.addRole(adminRole);
            
            // 创建普通用户角色
            Role userRole = new Role();
            userRole.setName("普通用户");
            userRole.setDescription("普通用户，拥有基本权限");
            userRole.setCode("ROLE_USER");
            userRole.setStatus("active");
            roleService.addRole(userRole);
            
            // 查找用户名为Admin的用户，将其设置为管理员
            User adminUser = userService.getUserByUsername("Admin");
            if (adminUser != null) {
                roleService.assignRoleToUser(adminUser.getUid(), adminRole.getId());
            }
            
            return Result.success("角色初始化成功");
        } catch (Exception e) {
            log.error("初始化角色失败", e);
            return Result.fail("初始化角色失败: " + e.getMessage());
        }
    }
} 