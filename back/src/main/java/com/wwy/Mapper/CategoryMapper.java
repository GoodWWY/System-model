package com.wwy.Mapper;

import com.wwy.Entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    /**
     * 根据ID获取分类
     */
    Category getCategoryById(@Param("id") Integer id);
    
    /**
     * 根据名称获取分类
     */
    Category getCategoryByName(@Param("name") String name);
    
    /**
     * 根据别名获取分类
     */
    Category getCategoryBySlug(@Param("slug") String slug);
    
    /**
     * 获取所有分类列表
     */
    List<Category> getAllCategories();
    
    /**
     * 获取启用的分类列表
     */
    List<Category> getEnabledCategories();
    
    /**
     * 根据父级ID获取分类列表
     */
    List<Category> getCategoriesByParent(@Param("parentId") Integer parentId);
    
    /**
     * 搜索分类
     */
    List<Category> searchCategories(@Param("keyword") String keyword,
                                   @Param("isEnabled") Boolean isEnabled,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);
    
    /**
     * 获取搜索分类总数
     */
    Integer countSearchCategories(@Param("keyword") String keyword,
                                 @Param("isEnabled") Boolean isEnabled);
    
    /**
     * 插入分类
     */
    Integer insertCategory(Category category);
    
    /**
     * 更新分类
     */
    Integer updateCategory(Category category);
    
    /**
     * 删除分类
     */
    Integer deleteCategory(@Param("id") Integer id);
    
    /**
     * 批量删除分类
     */
    Integer batchDeleteCategories(@Param("ids") List<Integer> ids);
    
    /**
     * 更新分类状态
     */
    Integer updateCategoryStatus(@Param("id") Integer id, @Param("isEnabled") Boolean isEnabled);
    
    /**
     * 更新分类文章数量
     */
    Integer updateArticleCount(@Param("id") Integer id, @Param("count") Integer count);
    
    /**
     * 增加分类文章数量
     */
    Integer incrementArticleCount(@Param("id") Integer id);
    
    /**
     * 减少分类文章数量
     */
    Integer decrementArticleCount(@Param("id") Integer id);
    
    /**
     * 获取分类树形结构
     */
    List<Category> getCategoryTree();
    
    /**
     * 检查分类名称是否存在
     */
    Integer checkCategoryNameExists(@Param("name") String name, @Param("excludeId") Integer excludeId);
    
    /**
     * 检查分类别名是否存在
     */
    Integer checkCategorySlugExists(@Param("slug") String slug, @Param("excludeId") Integer excludeId);
} 