package com.wwy.Service.Impl;

import com.wwy.Common.RedisUtil;
import com.wwy.Entity.Category;
import com.wwy.Mapper.CategoryMapper;
import com.wwy.Service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements ICategoryService {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    @Cacheable(value = "category", key = "#id")
    public Category getCategoryById(Integer id) {
        try {
            log.info("从数据库获取分类, id: {}", id);
            return categoryMapper.getCategoryById(id);
        } catch (Exception e) {
            log.error("获取分类失败, id: {}", id, e);
            return null;
        }
    }
    
    @Override
    public Category getCategoryByName(String name) {
        try {
            return categoryMapper.getCategoryByName(name);
        } catch (Exception e) {
            log.error("根据名称获取分类失败, name: {}", name, e);
            return null;
        }
    }
    
    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        try {
            log.info("从数据库获取所有分类");
            return categoryMapper.getAllCategories();
        } catch (Exception e) {
            log.error("获取所有分类失败", e);
            return null;
        }
    }
    
    @Override
    @Cacheable(value = "categories", key = "'enabled'")
    public List<Category> getEnabledCategories() {
        try {
            log.info("从数据库获取启用分类");
            return categoryMapper.getEnabledCategories();
        } catch (Exception e) {
            log.error("获取启用分类失败", e);
            return null;
        }
    }
    
    @Override
    public Map<String, Object> searchCategories(String keyword, Boolean isEnabled, 
                                                Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 设置默认值
            pageNum = pageNum != null ? pageNum : 1;
            pageSize = pageSize != null ? pageSize : 10;
            
            // 计算偏移量
            int offset = (pageNum - 1) * pageSize;
            
            // 查询分类列表
            List<Category> categories = categoryMapper.searchCategories(keyword, isEnabled, offset, pageSize);
            
            // 查询总数
            Integer total = categoryMapper.countSearchCategories(keyword, isEnabled);
            
            result.put("records", categories);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", (int) Math.ceil((double) total / pageSize));
            
            log.info("搜索分类成功, keyword: {}, total: {}", keyword, total);
            return result;
        } catch (Exception e) {
            log.error("搜索分类失败, keyword: {}", keyword, e);
            result.put("records", List.of());
            result.put("total", 0);
            return result;
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryStats"}, allEntries = true)
    public boolean createCategory(Category category) {
        try {
            // 检查名称是否已存在
            if (checkCategoryNameExists(category.getName(), null)) {
                log.warn("分类名称已存在: {}", category.getName());
                return false;
            }
            
            // 生成slug
            if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
                category.setSlug(generateSlug(category.getName()));
            }
            
            // 检查slug是否已存在
            if (checkCategorySlugExists(category.getSlug(), null)) {
                category.setSlug(category.getSlug() + "-" + System.currentTimeMillis());
            }
            
            // 设置默认值
            if (category.getParentId() == null) {
                category.setParentId(0);
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }
            if (category.getIsEnabled() == null) {
                category.setIsEnabled(true);
            }
            if (category.getArticleCount() == null) {
                category.setArticleCount(0);
            }
            
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            
            int result = categoryMapper.insertCategory(category);
            
            log.info("创建分类成功, id: {}, name: {}", category.getId(), category.getName());
            return result > 0;
        } catch (Exception e) {
            log.error("创建分类失败, name: {}", category.getName(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryStats", "category"}, allEntries = true)
    public boolean updateCategory(Category category) {
        try {
            // 检查分类是否存在
            Category existingCategory = categoryMapper.getCategoryById(category.getId());
            if (existingCategory == null) {
                log.warn("分类不存在, id: {}", category.getId());
                return false;
            }
            
            // 检查名称是否已存在（排除自己）
            if (checkCategoryNameExists(category.getName(), category.getId())) {
                log.warn("分类名称已存在: {}", category.getName());
                return false;
            }
            
            // 检查slug是否已存在（排除自己）
            if (category.getSlug() != null && checkCategorySlugExists(category.getSlug(), category.getId())) {
                log.warn("分类别名已存在: {}", category.getSlug());
                return false;
            }
            
            category.setUpdatedAt(LocalDateTime.now());
            
            int result = categoryMapper.updateCategory(category);
            
            log.info("更新分类成功, id: {}, name: {}", category.getId(), category.getName());
            return result > 0;
        } catch (Exception e) {
            log.error("更新分类失败, id: {}", category.getId(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryStats", "category"}, allEntries = true)
    public boolean deleteCategory(Integer id) {
        try {
            Category category = categoryMapper.getCategoryById(id);
            if (category == null) {
                log.warn("分类不存在, id: {}", id);
                return false;
            }
            
            // 检查是否有文章关联
            if (category.getArticleCount() != null && category.getArticleCount() > 0) {
                log.warn("分类下还有文章，无法删除, id: {}, articleCount: {}", id, category.getArticleCount());
                return false;
            }
            
            // 检查是否有子分类
            List<Category> children = categoryMapper.getCategoriesByParent(id);
            if (children != null && !children.isEmpty()) {
                log.warn("分类下还有子分类，无法删除, id: {}, childrenCount: {}", id, children.size());
                return false;
            }
            
            int result = categoryMapper.deleteCategory(id);
            
            log.info("删除分类成功, id: {}, name: {}", id, category.getName());
            return result > 0;
        } catch (Exception e) {
            log.error("删除分类失败, id: {}", id, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchDeleteCategories(List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }
            
            // 检查每个分类是否可以删除
            for (Integer id : ids) {
                Category category = categoryMapper.getCategoryById(id);
                if (category == null) {
                    continue;
                }
                
                // 检查是否有文章关联
                if (category.getArticleCount() != null && category.getArticleCount() > 0) {
                    log.warn("分类下还有文章，无法删除, id: {}, articleCount: {}", id, category.getArticleCount());
                    return false;
                }
                
                // 检查是否有子分类
                List<Category> children = categoryMapper.getCategoriesByParent(id);
                if (children != null && !children.isEmpty()) {
                    log.warn("分类下还有子分类，无法删除, id: {}, childrenCount: {}", id, children.size());
                    return false;
                }
            }
            
            int result = categoryMapper.batchDeleteCategories(ids);
            
            log.info("批量删除分类成功, count: {}", result);
            return result > 0;
        } catch (Exception e) {
            log.error("批量删除分类失败", e);
            return false;
        }
    }
    
    @Override
    @CacheEvict(value = {"categories", "categoryStats", "category"}, allEntries = true)
    public boolean updateCategoryStatus(Integer id, Boolean isEnabled) {
        try {
            int result = categoryMapper.updateCategoryStatus(id, isEnabled);
            log.info("更新分类状态成功, id: {}, isEnabled: {}", id, isEnabled);
            return result > 0;
        } catch (Exception e) {
            log.error("更新分类状态失败, id: {}, isEnabled: {}", id, isEnabled, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchUpdateCategoryStatus(List<Integer> ids, Boolean isEnabled) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }
            
            int successCount = 0;
            for (Integer id : ids) {
                if (updateCategoryStatus(id, isEnabled)) {
                    successCount++;
                }
            }
            
            log.info("批量更新分类状态成功, successCount: {}, total: {}", successCount, ids.size());
            return successCount > 0;
        } catch (Exception e) {
            log.error("批量更新分类状态失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateCategorySort(Integer id, Integer sortOrder) {
        try {
            Category category = categoryMapper.getCategoryById(id);
            if (category == null) {
                return false;
            }
            
            category.setSortOrder(sortOrder);
            category.setUpdatedAt(LocalDateTime.now());
            
            int result = categoryMapper.updateCategory(category);
            log.info("更新分类排序成功, id: {}, sortOrder: {}", id, sortOrder);
            return result > 0;
        } catch (Exception e) {
            log.error("更新分类排序失败, id: {}, sortOrder: {}", id, sortOrder, e);
            return false;
        }
    }
    
    @Override
    public boolean checkCategoryNameExists(String name, Integer excludeId) {
        try {
            Integer count = categoryMapper.checkCategoryNameExists(name, excludeId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查分类名称是否存在失败, name: {}", name, e);
            return false;
        }
    }
    
    @Override
    public boolean checkCategorySlugExists(String slug, Integer excludeId) {
        try {
            Integer count = categoryMapper.checkCategorySlugExists(slug, excludeId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查分类别名是否存在失败, slug: {}", slug, e);
            return false;
        }
    }
    
    @Override
    public List<Category> getCategoryTree() {
        try {
            return categoryMapper.getCategoryTree();
        } catch (Exception e) {
            log.error("获取分类树形结构失败", e);
            return null;
        }
    }
    
    @Override
    @Cacheable(value = "categoryStats", key = "'statistics'")
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        try {
            log.info("从数据库获取分类统计信息");
            
            // 总分类数
            Integer totalCount = categoryMapper.countSearchCategories(null, null);
            
            // 启用分类数
            Integer enabledCount = categoryMapper.countSearchCategories(null, true);
            
            // 禁用分类数
            Integer disabledCount = categoryMapper.countSearchCategories(null, false);
            
            statistics.put("totalCount", totalCount);
            statistics.put("enabledCount", enabledCount);
            statistics.put("disabledCount", disabledCount);
            
            log.info("获取分类统计信息成功");
            return statistics;
        } catch (Exception e) {
            log.error("获取分类统计信息失败", e);
            return statistics;
        }
    }
    
    /**
     * 生成URL友好的slug
     */
    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "category-" + System.currentTimeMillis();
        }
        
        return name.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
} 