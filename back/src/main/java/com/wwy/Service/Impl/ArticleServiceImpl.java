package com.wwy.Service.Impl;

import com.wwy.Entity.Article;
import com.wwy.Entity.Category;
import com.wwy.Mapper.ArticleMapper;
import com.wwy.Mapper.CategoryMapper;
import com.wwy.Service.IArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements IArticleService {
    
    private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public Article getArticleById(Integer id) {
        try {
            return articleMapper.getArticleById(id);
        } catch (Exception e) {
            log.error("获取文章失败, id: {}", id, e);
            return null;
        }
    }
    
    @Override
    public List<Article> getAllArticles() {
        try {
            return articleMapper.getAllArticles();
        } catch (Exception e) {
            log.error("获取所有文章失败", e);
            return null;
        }
    }
    
    @Override
    public Map<String, Object> searchArticles(String keyword, String status, Integer categoryId, 
                                              Integer authorId, Boolean isTop, Boolean isRecommend, 
                                              Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 设置默认值
            pageNum = pageNum != null ? pageNum : 1;
            pageSize = pageSize != null ? pageSize : 10;
            
            // 计算偏移量
            int offset = (pageNum - 1) * pageSize;
            
            // 查询文章列表
            List<Article> articles = articleMapper.searchArticles(keyword, status, categoryId, 
                                                                  authorId, isTop, isRecommend, 
                                                                  offset, pageSize);
            
            // 查询总数
            Integer total = articleMapper.countSearchArticles(keyword, status, categoryId, 
                                                              authorId, isTop, isRecommend);
            
            result.put("records", articles);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", (int) Math.ceil((double) total / pageSize));
            
            log.info("搜索文章成功, keyword: {}, total: {}", keyword, total);
            return result;
        } catch (Exception e) {
            log.error("搜索文章失败, keyword: {}", keyword, e);
            result.put("records", List.of());
            result.put("total", 0);
            return result;
        }
    }
    
    @Override
    @Transactional
    public boolean createArticle(Article article) {
        try {
            // 设置默认值
            if (article.getStatus() == null) {
                article.setStatus("draft");
            }
            if (article.getViewCount() == null) {
                article.setViewCount(0);
            }
            if (article.getLikeCount() == null) {
                article.setLikeCount(0);
            }
            if (article.getCommentCount() == null) {
                article.setCommentCount(0);
            }
            if (article.getIsTop() == null) {
                article.setIsTop(false);
            }
            if (article.getIsRecommend() == null) {
                article.setIsRecommend(false);
            }
            
            // 设置分类名称
            if (article.getCategoryId() != null) {
                Category category = categoryMapper.getCategoryById(article.getCategoryId());
                if (category != null) {
                    article.setCategoryName(category.getName());
                }
            }
            
            // 如果状态为发布，设置发布时间
            if ("published".equals(article.getStatus())) {
                article.setPublishTime(LocalDateTime.now());
            }
            
            int result = articleMapper.insertArticle(article);
            
            // 如果文章有分类，增加分类文章数量
            if (article.getCategoryId() != null && "published".equals(article.getStatus())) {
                categoryMapper.incrementArticleCount(article.getCategoryId());
            }
            
            log.info("创建文章成功, id: {}, title: {}", article.getId(), article.getTitle());
            return result > 0;
        } catch (Exception e) {
            log.error("创建文章失败, title: {}", article.getTitle(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateArticle(Article article) {
        try {
            // 获取原文章信息
            Article oldArticle = articleMapper.getArticleById(article.getId());
            if (oldArticle == null) {
                log.warn("文章不存在, id: {}", article.getId());
                return false;
            }
            
            // 设置分类名称
            if (article.getCategoryId() != null) {
                Category category = categoryMapper.getCategoryById(article.getCategoryId());
                if (category != null) {
                    article.setCategoryName(category.getName());
                }
            }
            
            // 如果状态改为发布，设置发布时间
            if ("published".equals(article.getStatus()) && !"published".equals(oldArticle.getStatus())) {
                article.setPublishTime(LocalDateTime.now());
            }
            
            int result = articleMapper.updateArticle(article);
            
            // 处理分类文章数量变更
            handleCategoryCountChange(oldArticle, article);
            
            log.info("更新文章成功, id: {}, title: {}", article.getId(), article.getTitle());
            return result > 0;
        } catch (Exception e) {
            log.error("更新文章失败, id: {}", article.getId(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteArticle(Integer id) {
        try {
            Article article = articleMapper.getArticleById(id);
            if (article == null) {
                log.warn("文章不存在, id: {}", id);
                return false;
            }
            
            int result = articleMapper.deleteArticle(id);
            
            // 如果文章有分类且已发布，减少分类文章数量
            if (article.getCategoryId() != null && "published".equals(article.getStatus())) {
                categoryMapper.decrementArticleCount(article.getCategoryId());
            }
            
            log.info("删除文章成功, id: {}, title: {}", id, article.getTitle());
            return result > 0;
        } catch (Exception e) {
            log.error("删除文章失败, id: {}", id, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchDeleteArticles(List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }
            
            // 获取要删除的文章信息
            List<Article> articles = ids.stream()
                    .map(articleMapper::getArticleById)
                    .filter(article -> article != null)
                    .toList();
            
            int result = articleMapper.batchDeleteArticles(ids);
            
            // 更新分类文章数量
            for (Article article : articles) {
                if (article.getCategoryId() != null && "published".equals(article.getStatus())) {
                    categoryMapper.decrementArticleCount(article.getCategoryId());
                }
            }
            
            log.info("批量删除文章成功, count: {}", result);
            return result > 0;
        } catch (Exception e) {
            log.error("批量删除文章失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean publishArticle(Integer id) {
        try {
            Article article = articleMapper.getArticleById(id);
            if (article == null) {
                return false;
            }
            
            int result = articleMapper.updateArticleStatus(id, "published");
            
            // 如果文章有分类且之前未发布，增加分类文章数量
            if (article.getCategoryId() != null && !"published".equals(article.getStatus())) {
                categoryMapper.incrementArticleCount(article.getCategoryId());
            }
            
            log.info("发布文章成功, id: {}", id);
            return result > 0;
        } catch (Exception e) {
            log.error("发布文章失败, id: {}", id, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean unpublishArticle(Integer id) {
        try {
            Article article = articleMapper.getArticleById(id);
            if (article == null) {
                return false;
            }
            
            int result = articleMapper.updateArticleStatus(id, "draft");
            
            // 如果文章有分类且已发布，减少分类文章数量
            if (article.getCategoryId() != null && "published".equals(article.getStatus())) {
                categoryMapper.decrementArticleCount(article.getCategoryId());
            }
            
            log.info("取消发布文章成功, id: {}", id);
            return result > 0;
        } catch (Exception e) {
            log.error("取消发布文章失败, id: {}", id, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean archiveArticle(Integer id) {
        try {
            Article article = articleMapper.getArticleById(id);
            if (article == null) {
                return false;
            }
            
            int result = articleMapper.updateArticleStatus(id, "archived");
            
            // 如果文章有分类且已发布，减少分类文章数量
            if (article.getCategoryId() != null && "published".equals(article.getStatus())) {
                categoryMapper.decrementArticleCount(article.getCategoryId());
            }
            
            log.info("归档文章成功, id: {}", id);
            return result > 0;
        } catch (Exception e) {
            log.error("归档文章失败, id: {}", id, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchUpdateArticleStatus(List<Integer> ids, String status) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }
            
            // 获取要更新的文章信息
            List<Article> articles = ids.stream()
                    .map(articleMapper::getArticleById)
                    .filter(article -> article != null)
                    .toList();
            
            int result = articleMapper.batchUpdateArticleStatus(ids, status);
            
            // 更新分类文章数量
            for (Article article : articles) {
                if (article.getCategoryId() != null) {
                    handleStatusChange(article, status);
                }
            }
            
            log.info("批量更新文章状态成功, count: {}, status: {}", result, status);
            return result > 0;
        } catch (Exception e) {
            log.error("批量更新文章状态失败, status: {}", status, e);
            return false;
        }
    }
    
    @Override
    public boolean incrementViewCount(Integer id) {
        try {
            int result = articleMapper.incrementViewCount(id);
            return result > 0;
        } catch (Exception e) {
            log.error("增加阅读量失败, id: {}", id, e);
            return false;
        }
    }
    
    @Override
    public boolean likeArticle(Integer id) {
        try {
            int result = articleMapper.incrementLikeCount(id);
            log.info("点赞文章成功, id: {}", id);
            return result > 0;
        } catch (Exception e) {
            log.error("点赞文章失败, id: {}", id, e);
            return false;
        }
    }
    
    @Override
    public boolean setTopArticle(Integer id, Boolean isTop) {
        try {
            int result = articleMapper.updateTopStatus(id, isTop);
            log.info("设置文章置顶成功, id: {}, isTop: {}", id, isTop);
            return result > 0;
        } catch (Exception e) {
            log.error("设置文章置顶失败, id: {}, isTop: {}", id, isTop, e);
            return false;
        }
    }
    
    @Override
    public boolean setRecommendArticle(Integer id, Boolean isRecommend) {
        try {
            int result = articleMapper.updateRecommendStatus(id, isRecommend);
            log.info("设置文章推荐成功, id: {}, isRecommend: {}", id, isRecommend);
            return result > 0;
        } catch (Exception e) {
            log.error("设置文章推荐失败, id: {}, isRecommend: {}", id, isRecommend, e);
            return false;
        }
    }
    
    @Override
    public List<Article> getArticlesByCategory(Integer categoryId) {
        try {
            return articleMapper.getArticlesByCategory(categoryId);
        } catch (Exception e) {
            log.error("根据分类获取文章失败, categoryId: {}", categoryId, e);
            return null;
        }
    }
    
    @Override
    public List<Article> getArticlesByAuthor(Integer authorId) {
        try {
            return articleMapper.getArticlesByAuthor(authorId);
        } catch (Exception e) {
            log.error("根据作者获取文章失败, authorId: {}", authorId, e);
            return null;
        }
    }
    
    @Override
    public List<Article> getHotArticles(Integer limit) {
        try {
            return articleMapper.getHotArticles(limit);
        } catch (Exception e) {
            log.error("获取热门文章失败, limit: {}", limit, e);
            return null;
        }
    }
    
    @Override
    public List<Article> getRecommendArticles(Integer limit) {
        try {
            return articleMapper.getRecommendArticles(limit);
        } catch (Exception e) {
            log.error("获取推荐文章失败, limit: {}", limit, e);
            return null;
        }
    }
    
    @Override
    public List<Article> getTopArticles() {
        try {
            return articleMapper.getTopArticles();
        } catch (Exception e) {
            log.error("获取置顶文章失败", e);
            return null;
        }
    }
    
    @Override
    public Map<String, Object> getArticleStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        try {
            // 总文章数
            Integer totalCount = articleMapper.countSearchArticles(null, null, null, null, null, null);
            
            // 已发布文章数
            Integer publishedCount = articleMapper.countSearchArticles(null, "published", null, null, null, null);
            
            // 草稿数
            Integer draftCount = articleMapper.countSearchArticles(null, "draft", null, null, null, null);
            
            // 归档数
            Integer archivedCount = articleMapper.countSearchArticles(null, "archived", null, null, null, null);
            
            // 置顶文章数
            Integer topCount = articleMapper.countSearchArticles(null, null, null, null, true, null);
            
            // 推荐文章数
            Integer recommendCount = articleMapper.countSearchArticles(null, null, null, null, null, true);
            
            statistics.put("totalCount", totalCount);
            statistics.put("publishedCount", publishedCount);
            statistics.put("draftCount", draftCount);
            statistics.put("archivedCount", archivedCount);
            statistics.put("topCount", topCount);
            statistics.put("recommendCount", recommendCount);
            
            log.info("获取文章统计信息成功");
            return statistics;
        } catch (Exception e) {
            log.error("获取文章统计信息失败", e);
            return statistics;
        }
    }
    
    /**
     * 处理分类文章数量变更
     */
    private void handleCategoryCountChange(Article oldArticle, Article newArticle) {
        Integer oldCategoryId = oldArticle.getCategoryId();
        Integer newCategoryId = newArticle.getCategoryId();
        String oldStatus = oldArticle.getStatus();
        String newStatus = newArticle.getStatus();
        
        // 分类没有变化
        if (oldCategoryId != null && oldCategoryId.equals(newCategoryId)) {
            // 只是状态变化
            handleStatusChange(oldArticle, newStatus);
            return;
        }
        
        // 分类发生变化
        if (oldCategoryId != null && "published".equals(oldStatus)) {
            categoryMapper.decrementArticleCount(oldCategoryId);
        }
        
        if (newCategoryId != null && "published".equals(newStatus)) {
            categoryMapper.incrementArticleCount(newCategoryId);
        }
    }
    
    /**
     * 处理状态变更对分类文章数量的影响
     */
    private void handleStatusChange(Article article, String newStatus) {
        if (article.getCategoryId() == null) {
            return;
        }
        
        String oldStatus = article.getStatus();
        
        // 从非发布状态变为发布状态
        if (!"published".equals(oldStatus) && "published".equals(newStatus)) {
            categoryMapper.incrementArticleCount(article.getCategoryId());
        }
        // 从发布状态变为非发布状态
        else if ("published".equals(oldStatus) && !"published".equals(newStatus)) {
            categoryMapper.decrementArticleCount(article.getCategoryId());
        }
    }
} 