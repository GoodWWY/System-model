package com.wwy.Service;

import com.wwy.Entity.Category;

import java.util.List;
import java.util.Map;

public interface ICategoryService {
    
    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Integer id);
    
    /**
     * 根据名称获取分类
     */
    Category getCategoryByName(String name);
    
    /**
     * 获取所有分类列表
     */
    List<Category> getAllCategories();
    
    /**
     * 获取启用的分类列表
     */
    List<Category> getEnabledCategories();
    
    /**
     * 分页搜索分类
     */
    Map<String, Object> searchCategories(String keyword, Boolean isEnabled, 
                                         Integer pageNum, Integer pageSize);
    
    /**
     * 创建分类
     */
    boolean createCategory(Category category);
    
    /**
     * 更新分类
     */
    boolean updateCategory(Category category);
    
    /**
     * 删除分类
     */
    boolean deleteCategory(Integer id);
    
    /**
     * 批量删除分类
     */
    boolean batchDeleteCategories(List<Integer> ids);
    
    /**
     * 更新分类状态
     */
    boolean updateCategoryStatus(Integer id, Boolean isEnabled);
    
    /**
     * 批量更新分类状态
     */
    boolean batchUpdateCategoryStatus(List<Integer> ids, Boolean isEnabled);
    
    /**
     * 更新分类排序
     */
    boolean updateCategorySort(Integer id, Integer sortOrder);
    
    /**
     * 检查分类名称是否存在
     */
    boolean checkCategoryNameExists(String name, Integer excludeId);
    
    /**
     * 检查分类别名是否存在
     */
    boolean checkCategorySlugExists(String slug, Integer excludeId);
    
    /**
     * 获取分类树形结构
     */
    List<Category> getCategoryTree();
    
    /**
     * 获取分类统计信息
     */
    Map<String, Object> getCategoryStatistics();
} 