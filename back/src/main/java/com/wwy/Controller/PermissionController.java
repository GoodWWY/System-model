package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Entity.Permission;
import com.wwy.Service.IPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/permission")
@CrossOrigin
public class PermissionController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    @Autowired
    private IPermissionService permissionService;

    /**
     * 获取权限列表（分页搜索）
     */
    @GetMapping("/list")
    public Result getPermissionList(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "") String category,
                                  @RequestParam(required = false) Boolean isEnabled,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = permissionService.searchPermissions(keyword, category, isEnabled, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取权限列表失败", e);
            return Result.fail("获取权限列表失败");
        }
    }

    /**
     * 获取权限树形结构
     */
    @GetMapping("/tree")
    public Result getPermissionTree() {
        try {
            List<Permission> tree = permissionService.getPermissionTree();
            return Result.success(tree);
        } catch (Exception e) {
            logger.error("获取权限树失败", e);
            return Result.fail("获取权限树失败");
        }
    }

    /**
     * 根据角色ID获取权限列表
     */
    @GetMapping("/role/{roleId}")
    public Result getPermissionsByRoleId(@PathVariable Integer roleId) {
        try {
            List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
            return Result.success(permissions);
        } catch (Exception e) {
            logger.error("根据角色ID获取权限失败，角色ID: {}", roleId, e);
            return Result.fail("获取角色权限失败");
        }
    }

    /**
     * 根据用户ID获取权限列表
     */
    @GetMapping("/user/{userId}")
    public Result getPermissionsByUserId(@PathVariable Integer userId) {
        try {
            List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
            return Result.success(permissions);
        } catch (Exception e) {
            logger.error("根据用户ID获取权限失败，用户ID: {}", userId, e);
            return Result.fail("获取用户权限失败");
        }
    }

    /**
     * 根据ID获取权限详情
     */
    @GetMapping("/{id}")
    public Result getPermissionById(@PathVariable Integer id) {
        try {
            Permission permission = permissionService.getPermissionById(id);
            if (permission != null) {
                return Result.success(permission);
            } else {
                return Result.fail("权限不存在");
            }
        } catch (Exception e) {
            logger.error("获取权限详情失败，ID: {}", id, e);
            return Result.fail("获取权限详情失败");
        }
    }

    /**
     * 创建权限
     */
    @PostMapping
    public Result createPermission(@RequestBody Permission permission) {
        try {
            // 参数验证
            if (permission.getName() == null || permission.getName().trim().isEmpty()) {
                return Result.fail("权限名称不能为空");
            }
            if (permission.getCode() == null || permission.getCode().trim().isEmpty()) {
                return Result.fail("权限编码不能为空");
            }

            boolean success = permissionService.createPermission(permission);
            if (success) {
                return Result.success("权限创建成功");
            } else {
                return Result.fail("权限创建失败，可能权限编码已存在");
            }
        } catch (Exception e) {
            logger.error("创建权限失败", e);
            return Result.fail("创建权限失败");
        }
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    public Result updatePermission(@PathVariable Integer id, @RequestBody Permission permission) {
        try {
            permission.setId(id);
            
            // 参数验证
            if (permission.getName() == null || permission.getName().trim().isEmpty()) {
                return Result.fail("权限名称不能为空");
            }
            if (permission.getCode() == null || permission.getCode().trim().isEmpty()) {
                return Result.fail("权限编码不能为空");
            }

            boolean success = permissionService.updatePermission(permission);
            if (success) {
                return Result.success("权限更新成功");
            } else {
                return Result.fail("权限更新失败，可能权限编码已存在");
            }
        } catch (Exception e) {
            logger.error("更新权限失败，ID: {}", id, e);
            return Result.fail("更新权限失败");
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public Result deletePermission(@PathVariable Integer id) {
        try {
            boolean success = permissionService.deletePermission(id);
            if (success) {
                return Result.success("权限删除成功");
            } else {
                return Result.fail("权限删除失败，可能权限下有子权限");
            }
        } catch (Exception e) {
            logger.error("删除权限失败，ID: {}", id, e);
            return Result.fail("删除权限失败");
        }
    }

    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    public Result batchDeletePermissions(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.fail("请选择要删除的权限");
            }

            boolean success = permissionService.batchDeletePermissions(ids);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.fail("批量删除失败");
            }
        } catch (Exception e) {
            logger.error("批量删除权限失败", e);
            return Result.fail("批量删除失败");
        }
    }

    /**
     * 更新权限状态
     */
    @PutMapping("/{id}/status")
    public Result updatePermissionStatus(@PathVariable Integer id, @RequestParam Boolean isEnabled) {
        try {
            boolean success = permissionService.updatePermissionStatus(id, isEnabled);
            if (success) {
                return Result.success("权限状态更新成功");
            } else {
                return Result.fail("权限状态更新失败");
            }
        } catch (Exception e) {
            logger.error("更新权限状态失败，ID: {}", id, e);
            return Result.fail("更新权限状态失败");
        }
    }

    /**
     * 批量更新权限状态
     */
    @PutMapping("/batch/status")
    public Result batchUpdatePermissionStatus(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) params.get("ids");
            Boolean isEnabled = (Boolean) params.get("isEnabled");

            if (ids == null || ids.isEmpty()) {
                return Result.fail("请选择要更新的权限");
            }

            boolean success = permissionService.batchUpdatePermissionStatus(ids, isEnabled);
            if (success) {
                return Result.success("批量更新成功");
            } else {
                return Result.fail("批量更新失败");
            }
        } catch (Exception e) {
            logger.error("批量更新权限状态失败", e);
            return Result.fail("批量更新失败");
        }
    }

    /**
     * 检查权限编码是否存在
     */
    @GetMapping("/check-code")
    public Result checkPermissionCode(@RequestParam String code, @RequestParam(required = false) Integer excludeId) {
        try {
            boolean exists = permissionService.checkPermissionCodeExists(code, excludeId);
            return Result.success(exists);
        } catch (Exception e) {
            logger.error("检查权限编码失败，编码: {}", code, e);
            return Result.fail("检查权限编码失败");
        }
    }

    /**
     * 获取权限统计信息
     */
    @GetMapping("/statistics")
    public Result getPermissionStatistics() {
        try {
            Map<String, Object> stats = permissionService.getPermissionStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取权限统计信息失败", e);
            return Result.fail("获取权限统计信息失败");
        }
    }

    /**
     * 为角色分配权限
     */
    @PostMapping("/assign-role")
    public Result assignPermissionsToRole(@RequestBody Map<String, Object> params) {
        try {
            Integer roleId = (Integer) params.get("roleId");
            @SuppressWarnings("unchecked")
            List<Integer> permissionIds = (List<Integer>) params.get("permissionIds");

            if (roleId == null) {
                return Result.fail("角色ID不能为空");
            }

            boolean success = permissionService.assignPermissionsToRole(roleId, permissionIds);
            if (success) {
                return Result.success("权限分配成功");
            } else {
                return Result.fail("权限分配失败");
            }
        } catch (Exception e) {
            logger.error("为角色分配权限失败", e);
            return Result.fail("权限分配失败");
        }
    }

    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/check-user-permission")
    public Result checkUserPermission(@RequestParam Integer userId, @RequestParam String permissionCode) {
        try {
            boolean hasPermission = permissionService.hasPermission(userId, permissionCode);
            return Result.success(hasPermission);
        } catch (Exception e) {
            logger.error("检查用户权限失败，用户ID: {}, 权限编码: {}", userId, permissionCode, e);
            return Result.fail("检查用户权限失败");
        }
    }

    /**
     * 获取用户权限编码列表
     */
    @GetMapping("/user-codes/{userId}")
    public Result getUserPermissionCodes(@PathVariable Integer userId) {
        try {
            List<String> permissionCodes = permissionService.getUserPermissionCodes(userId);
            return Result.success(permissionCodes);
        } catch (Exception e) {
            logger.error("获取用户权限编码失败，用户ID: {}", userId, e);
            return Result.fail("获取用户权限编码失败");
        }
    }
} 