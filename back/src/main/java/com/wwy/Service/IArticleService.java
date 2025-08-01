package com.wwy.Service;

import com.wwy.Entity.Article;

import java.util.List;
import java.util.Map;

public interface IArticleService {
    
    /**
     * 根据ID获取文章
     */
    Article getArticleById(Integer id);
    
    /**
     * 获取所有文章列表
     */
    List<Article> getAllArticles();
    
    /**
     * 分页搜索文章
     */
    Map<String, Object> searchArticles(String keyword, String status, Integer categoryId, 
                                       Integer authorId, Boolean isTop, Boolean isRecommend, 
                                       Integer pageNum, Integer pageSize);
    
    /**
     * 创建文章
     */
    boolean createArticle(Article article);
    
    /**
     * 更新文章
     */
    boolean updateArticle(Article article);
    
    /**
     * 删除文章
     */
    boolean deleteArticle(Integer id);
    
    /**
     * 批量删除文章
     */
    boolean batchDeleteArticles(List<Integer> ids);
    
    /**
     * 发布文章
     */
    boolean publishArticle(Integer id);
    
    /**
     * 取消发布文章
     */
    boolean unpublishArticle(Integer id);
    
    /**
     * 归档文章
     */
    boolean archiveArticle(Integer id);
    
    /**
     * 批量更新文章状态
     */
    boolean batchUpdateArticleStatus(List<Integer> ids, String status);
    
    /**
     * 增加文章阅读量
     */
    boolean incrementViewCount(Integer id);
    
    /**
     * 点赞文章
     */
    boolean likeArticle(Integer id);
    
    /**
     * 设置文章置顶
     */
    boolean setTopArticle(Integer id, Boolean isTop);
    
    /**
     * 设置文章推荐
     */
    boolean setRecommendArticle(Integer id, Boolean isRecommend);
    
    /**
     * 根据分类获取文章列表
     */
    List<Article> getArticlesByCategory(Integer categoryId);
    
    /**
     * 根据作者获取文章列表
     */
    List<Article> getArticlesByAuthor(Integer authorId);
    
    /**
     * 获取热门文章
     */
    List<Article> getHotArticles(Integer limit);
    
    /**
     * 获取推荐文章
     */
    List<Article> getRecommendArticles(Integer limit);
    
    /**
     * 获取置顶文章
     */
    List<Article> getTopArticles();
    
    /**
     * 获取文章统计信息
     */
    Map<String, Object> getArticleStatistics();
} 