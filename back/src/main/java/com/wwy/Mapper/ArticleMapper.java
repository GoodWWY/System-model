package com.wwy.Mapper;

import com.wwy.Entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    
    /**
     * 根据ID获取文章
     */
    Article getArticleById(@Param("id") Integer id);
    
    /**
     * 获取所有文章列表
     */
    List<Article> getAllArticles();
    
    /**
     * 分页搜索文章
     */
    List<Article> searchArticles(@Param("keyword") String keyword,
                                @Param("status") String status,
                                @Param("categoryId") Integer categoryId,
                                @Param("authorId") Integer authorId,
                                @Param("isTop") Boolean isTop,
                                @Param("isRecommend") Boolean isRecommend,
                                @Param("offset") Integer offset,
                                @Param("pageSize") Integer pageSize);
    
    /**
     * 获取搜索文章总数
     */
    Integer countSearchArticles(@Param("keyword") String keyword,
                               @Param("status") String status,
                               @Param("categoryId") Integer categoryId,
                               @Param("authorId") Integer authorId,
                               @Param("isTop") Boolean isTop,
                               @Param("isRecommend") Boolean isRecommend);
    
    /**
     * 插入文章
     */
    Integer insertArticle(Article article);
    
    /**
     * 更新文章
     */
    Integer updateArticle(Article article);
    
    /**
     * 删除文章
     */
    Integer deleteArticle(@Param("id") Integer id);
    
    /**
     * 批量删除文章
     */
    Integer batchDeleteArticles(@Param("ids") List<Integer> ids);
    
    /**
     * 更新文章状态
     */
    Integer updateArticleStatus(@Param("id") Integer id, @Param("status") String status);
    
    /**
     * 批量更新文章状态
     */
    Integer batchUpdateArticleStatus(@Param("ids") List<Integer> ids, @Param("status") String status);
    
    /**
     * 增加文章阅读量
     */
    Integer incrementViewCount(@Param("id") Integer id);
    
    /**
     * 增加文章点赞数
     */
    Integer incrementLikeCount(@Param("id") Integer id);
    
    /**
     * 更新文章置顶状态
     */
    Integer updateTopStatus(@Param("id") Integer id, @Param("isTop") Boolean isTop);
    
    /**
     * 更新文章推荐状态
     */
    Integer updateRecommendStatus(@Param("id") Integer id, @Param("isRecommend") Boolean isRecommend);
    
    /**
     * 根据分类ID获取文章列表
     */
    List<Article> getArticlesByCategory(@Param("categoryId") Integer categoryId);
    
    /**
     * 根据作者ID获取文章列表
     */
    List<Article> getArticlesByAuthor(@Param("authorId") Integer authorId);
    
    /**
     * 获取热门文章（按阅读量排序）
     */
    List<Article> getHotArticles(@Param("limit") Integer limit);
    
    /**
     * 获取推荐文章
     */
    List<Article> getRecommendArticles(@Param("limit") Integer limit);
    
    /**
     * 获取置顶文章
     */
    List<Article> getTopArticles();
} 