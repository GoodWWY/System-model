package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Entity.Category;
import com.wwy.Service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
@CrossOrigin
public class CategoryController {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    
    @Autowired
    private ICategoryService categoryService;
    
    /**
     * 分页搜索分类
     */
    @GetMapping("/search")
    public Result searchCategories(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Boolean isEnabled,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            log.info("搜索分类请求: keyword={}, isEnabled={}, pageNum={}, pageSize={}", 
                    keyword, isEnabled, pageNum, pageSize);
            
            Map<String, Object> result = categoryService.searchCategories(keyword, isEnabled, pageNum, pageSize);
            
            log.info("搜索分类成功: total={}", result.get("total"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索分类失败", e);
            return Result.fail("搜索分类失败");
        }
    }
    
    /**
     * 获取所有分类列表
     */
    @GetMapping("/list")
    public Result getAllCategories() {
        try {
            log.info("获取所有分类列表请求");
            
            List<Category> categories = categoryService.getAllCategories();
            
            log.info("获取所有分类列表成功: count={}", categories != null ? categories.size() : 0);
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取所有分类列表失败", e);
            return Result.fail("获取分类列表失败");
        }
    }
    
    /**
     * 获取启用的分类列表
     */
    @GetMapping("/enabled")
    public Result getEnabledCategories() {
        try {
            log.info("获取启用分类列表请求");
            
            List<Category> categories = categoryService.getEnabledCategories();
            
            log.info("获取启用分类列表成功: count={}", categories != null ? categories.size() : 0);
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取启用分类列表失败", e);
            return Result.fail("获取启用分类列表失败");
        }
    }
    
    /**
     * 获取分类树形结构
     */
    @GetMapping("/tree")
    public Result getCategoryTree() {
        try {
            log.info("获取分类树形结构请求");
            
            List<Category> categories = categoryService.getCategoryTree();
            
            log.info("获取分类树形结构成功: count={}", categories != null ? categories.size() : 0);
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取分类树形结构失败", e);
            return Result.fail("获取分类树形结构失败");
        }
    }
    
    /**
     * 根据ID获取分类
     */
    @GetMapping("/{id}")
    public Result getCategoryById(@PathVariable Integer id) {
        try {
            log.info("获取分类详情请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("分类ID不能为空");
            }
            
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                return Result.fail("分类不存在");
            }
            
            log.info("获取分类详情成功: id={}, name={}", id, category.getName());
            return Result.success(category);
        } catch (Exception e) {
            log.error("获取分类详情失败", e);
            return Result.fail("获取分类详情失败");
        }
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    public Result createCategory(@RequestBody Category category) {
        try {
            log.info("创建分类请求: name={}", category.getName());
            
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                return Result.fail("分类名称不能为空");
            }
            
            boolean success = categoryService.createCategory(category);
            if (success) {
                log.info("创建分类成功: id={}, name={}", category.getId(), category.getName());
                return Result.success(category);
            } else {
                return Result.fail("分类名称已存在或创建失败");
            }
        } catch (Exception e) {
            log.error("创建分类失败", e);
            return Result.fail("创建分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        try {
            log.info("更新分类请求: id={}, name={}", id, category.getName());
            
            if (id == null || id <= 0) {
                return Result.fail("分类ID不能为空");
            }
            
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                return Result.fail("分类名称不能为空");
            }
            
            category.setId(id);
            boolean success = categoryService.updateCategory(category);
            if (success) {
                log.info("更新分类成功: id={}, name={}", id, category.getName());
                return Result.success("更新成功");
            } else {
                return Result.fail("分类名称已存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return Result.fail("更新分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Integer id) {
        try {
            log.info("删除分类请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("分类ID不能为空");
            }
            
            boolean success = categoryService.deleteCategory(id);
            if (success) {
                log.info("删除分类成功: id={}", id);
                return Result.success("删除成功");
            } else {
                return Result.fail("分类下还有文章或子分类，无法删除");
            }
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return Result.fail("删除分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    public Result batchDeleteCategories(@RequestBody List<Integer> ids) {
        try {
            log.info("批量删除分类请求: ids={}", ids);
            
            if (ids == null || ids.isEmpty()) {
                return Result.fail("分类ID列表不能为空");
            }
            
            boolean success = categoryService.batchDeleteCategories(ids);
            if (success) {
                log.info("批量删除分类成功: count={}", ids.size());
                return Result.success("批量删除成功");
            } else {
                return Result.fail("部分分类下还有文章或子分类，无法删除");
            }
        } catch (Exception e) {
            log.error("批量删除分类失败", e);
            return Result.fail("批量删除分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新分类状态
     */
    @PutMapping("/{id}/status")
    public Result updateCategoryStatus(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isEnabled = request.get("isEnabled");
            log.info("更新分类状态请求: id={}, isEnabled={}", id, isEnabled);
            
            if (id == null || id <= 0) {
                return Result.fail("分类ID不能为空");
            }
            
            if (isEnabled == null) {
                return Result.fail("状态不能为空");
            }
            
            boolean success = categoryService.updateCategoryStatus(id, isEnabled);
            if (success) {
                log.info("更新分类状态成功: id={}, isEnabled={}", id, isEnabled);
                return Result.success(isEnabled ? "启用成功" : "禁用成功");
            } else {
                return Result.fail("更新分类状态失败");
            }
        } catch (Exception e) {
            log.error("更新分类状态失败", e);
            return Result.fail("更新分类状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新分类状态
     */
    @PutMapping("/batch/status")
    public Result batchUpdateCategoryStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) request.get("ids");
            Boolean isEnabled = (Boolean) request.get("isEnabled");
            
            log.info("批量更新分类状态请求: ids={}, isEnabled={}", ids, isEnabled);
            
            if (ids == null || ids.isEmpty()) {
                return Result.fail("分类ID列表不能为空");
            }
            
            if (isEnabled == null) {
                return Result.fail("状态不能为空");
            }
            
            boolean success = categoryService.batchUpdateCategoryStatus(ids, isEnabled);
            if (success) {
                log.info("批量更新分类状态成功: count={}, isEnabled={}", ids.size(), isEnabled);
                return Result.success("批量更新状态成功");
            } else {
                return Result.fail("批量更新分类状态失败");
            }
        } catch (Exception e) {
            log.error("批量更新分类状态失败", e);
            return Result.fail("批量更新分类状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新分类排序
     */
    @PutMapping("/{id}/sort")
    public Result updateCategorySort(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            Integer sortOrder = request.get("sortOrder");
            log.info("更新分类排序请求: id={}, sortOrder={}", id, sortOrder);
            
            if (id == null || id <= 0) {
                return Result.fail("分类ID不能为空");
            }
            
            if (sortOrder == null) {
                return Result.fail("排序值不能为空");
            }
            
            boolean success = categoryService.updateCategorySort(id, sortOrder);
            if (success) {
                log.info("更新分类排序成功: id={}, sortOrder={}", id, sortOrder);
                return Result.success("更新排序成功");
            } else {
                return Result.fail("更新分类排序失败");
            }
        } catch (Exception e) {
            log.error("更新分类排序失败", e);
            return Result.fail("更新分类排序失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查分类名称是否存在
     */
    @GetMapping("/check-name")
    public Result checkCategoryNameExists(@RequestParam String name, 
                                         @RequestParam(required = false) Integer excludeId) {
        try {
            log.info("检查分类名称请求: name={}, excludeId={}", name, excludeId);
            
            if (name == null || name.trim().isEmpty()) {
                return Result.fail("分类名称不能为空");
            }
            
            boolean exists = categoryService.checkCategoryNameExists(name, excludeId);
            
            log.info("检查分类名称结果: name={}, exists={}", name, exists);
            return Result.success(Map.of("exists", exists));
        } catch (Exception e) {
            log.error("检查分类名称失败", e);
            return Result.fail("检查分类名称失败");
        }
    }
    
    /**
     * 获取分类统计信息
     */
    @GetMapping("/statistics")
    public Result getCategoryStatistics() {
        try {
            log.info("获取分类统计信息请求");
            
            Map<String, Object> statistics = categoryService.getCategoryStatistics();
            
            log.info("获取分类统计信息成功");
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取分类统计信息失败", e);
            return Result.fail("获取分类统计信息失败");
        }
    }
} 