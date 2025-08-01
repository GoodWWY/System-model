package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Entity.Article;
import com.wwy.Service.IArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/article")
@CrossOrigin
public class ArticleController {
    
    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);
    
    @Autowired
    private IArticleService articleService;
    
    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result getArticleById(@PathVariable Integer id) {
        try {
            log.info("获取文章详情请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            Article article = articleService.getArticleById(id);
            if (article == null) {
                return Result.fail("文章不存在");
            }
            
            // 增加阅读量
            articleService.incrementViewCount(id);
            
            log.info("获取文章详情成功: id={}, title={}", id, article.getTitle());
            return Result.success(article);
        } catch (Exception e) {
            log.error("获取文章详情失败", e);
            return Result.fail("获取文章详情失败");
        }
    }
    
    /**
     * 分页搜索文章
     */
    @GetMapping("/search")
    public Result searchArticles(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) Integer categoryId,
                                @RequestParam(required = false) Integer authorId,
                                @RequestParam(required = false) Boolean isTop,
                                @RequestParam(required = false) Boolean isRecommend,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            log.info("搜索文章请求: keyword={}, status={}, categoryId={}, pageNum={}, pageSize={}", 
                    keyword, status, categoryId, pageNum, pageSize);
            
            Map<String, Object> result = articleService.searchArticles(keyword, status, categoryId, 
                                                                       authorId, isTop, isRecommend, 
                                                                       pageNum, pageSize);
            
            log.info("搜索文章成功: total={}", result.get("total"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索文章失败", e);
            return Result.fail("搜索文章失败");
        }
    }
    
    /**
     * 创建文章
     */
    @PostMapping
    public Result createArticle(@RequestBody Article article) {
        try {
            log.info("创建文章请求: title={}", article.getTitle());
            
            if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
                return Result.fail("文章标题不能为空");
            }
            
            if (article.getContent() == null || article.getContent().trim().isEmpty()) {
                return Result.fail("文章内容不能为空");
            }
            
            if (article.getAuthorId() == null) {
                return Result.fail("作者ID不能为空");
            }
            
            boolean success = articleService.createArticle(article);
            if (success) {
                log.info("创建文章成功: id={}, title={}", article.getId(), article.getTitle());
                return Result.success(article);
            } else {
                return Result.fail("创建文章失败");
            }
        } catch (Exception e) {
            log.error("创建文章失败", e);
            return Result.fail("创建文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public Result updateArticle(@PathVariable Integer id, @RequestBody Article article) {
        try {
            log.info("更新文章请求: id={}, title={}", id, article.getTitle());
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
                return Result.fail("文章标题不能为空");
            }
            
            if (article.getContent() == null || article.getContent().trim().isEmpty()) {
                return Result.fail("文章内容不能为空");
            }
            
            article.setId(id);
            boolean success = articleService.updateArticle(article);
            if (success) {
                log.info("更新文章成功: id={}, title={}", id, article.getTitle());
                return Result.success("更新成功");
            } else {
                return Result.fail("更新文章失败");
            }
        } catch (Exception e) {
            log.error("更新文章失败", e);
            return Result.fail("更新文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result deleteArticle(@PathVariable Integer id) {
        try {
            log.info("删除文章请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            boolean success = articleService.deleteArticle(id);
            if (success) {
                log.info("删除文章成功: id={}", id);
                return Result.success("删除成功");
            } else {
                return Result.fail("删除文章失败");
            }
        } catch (Exception e) {
            log.error("删除文章失败", e);
            return Result.fail("删除文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除文章
     */
    @DeleteMapping("/batch")
    public Result batchDeleteArticles(@RequestBody List<Integer> ids) {
        try {
            log.info("批量删除文章请求: ids={}", ids);
            
            if (ids == null || ids.isEmpty()) {
                return Result.fail("文章ID列表不能为空");
            }
            
            boolean success = articleService.batchDeleteArticles(ids);
            if (success) {
                log.info("批量删除文章成功: count={}", ids.size());
                return Result.success("批量删除成功");
            } else {
                return Result.fail("批量删除文章失败");
            }
        } catch (Exception e) {
            log.error("批量删除文章失败", e);
            return Result.fail("批量删除文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 发布文章
     */
    @PutMapping("/{id}/publish")
    public Result publishArticle(@PathVariable Integer id) {
        try {
            log.info("发布文章请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            boolean success = articleService.publishArticle(id);
            if (success) {
                log.info("发布文章成功: id={}", id);
                return Result.success("发布成功");
            } else {
                return Result.fail("发布文章失败");
            }
        } catch (Exception e) {
            log.error("发布文章失败", e);
            return Result.fail("发布文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消发布文章
     */
    @PutMapping("/{id}/unpublish")
    public Result unpublishArticle(@PathVariable Integer id) {
        try {
            log.info("取消发布文章请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            boolean success = articleService.unpublishArticle(id);
            if (success) {
                log.info("取消发布文章成功: id={}", id);
                return Result.success("取消发布成功");
            } else {
                return Result.fail("取消发布文章失败");
            }
        } catch (Exception e) {
            log.error("取消发布文章失败", e);
            return Result.fail("取消发布文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 归档文章
     */
    @PutMapping("/{id}/archive")
    public Result archiveArticle(@PathVariable Integer id) {
        try {
            log.info("归档文章请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            boolean success = articleService.archiveArticle(id);
            if (success) {
                log.info("归档文章成功: id={}", id);
                return Result.success("归档成功");
            } else {
                return Result.fail("归档文章失败");
            }
        } catch (Exception e) {
            log.error("归档文章失败", e);
            return Result.fail("归档文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新文章状态
     */
    @PutMapping("/batch/status")
    public Result batchUpdateArticleStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) request.get("ids");
            String status = (String) request.get("status");
            
            log.info("批量更新文章状态请求: ids={}, status={}", ids, status);
            
            if (ids == null || ids.isEmpty()) {
                return Result.fail("文章ID列表不能为空");
            }
            
            if (status == null || status.trim().isEmpty()) {
                return Result.fail("状态不能为空");
            }
            
            boolean success = articleService.batchUpdateArticleStatus(ids, status);
            if (success) {
                log.info("批量更新文章状态成功: count={}, status={}", ids.size(), status);
                return Result.success("批量更新状态成功");
            } else {
                return Result.fail("批量更新文章状态失败");
            }
        } catch (Exception e) {
            log.error("批量更新文章状态失败", e);
            return Result.fail("批量更新文章状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 点赞文章
     */
    @PutMapping("/{id}/like")
    public Result likeArticle(@PathVariable Integer id) {
        try {
            log.info("点赞文章请求: id={}", id);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            boolean success = articleService.likeArticle(id);
            if (success) {
                log.info("点赞文章成功: id={}", id);
                return Result.success("点赞成功");
            } else {
                return Result.fail("点赞文章失败");
            }
        } catch (Exception e) {
            log.error("点赞文章失败", e);
            return Result.fail("点赞文章失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置文章置顶
     */
    @PutMapping("/{id}/top")
    public Result setTopArticle(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isTop = request.get("isTop");
            log.info("设置文章置顶请求: id={}, isTop={}", id, isTop);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            if (isTop == null) {
                return Result.fail("置顶状态不能为空");
            }
            
            boolean success = articleService.setTopArticle(id, isTop);
            if (success) {
                log.info("设置文章置顶成功: id={}, isTop={}", id, isTop);
                return Result.success(isTop ? "设置置顶成功" : "取消置顶成功");
            } else {
                return Result.fail("设置文章置顶失败");
            }
        } catch (Exception e) {
            log.error("设置文章置顶失败", e);
            return Result.fail("设置文章置顶失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置文章推荐
     */
    @PutMapping("/{id}/recommend")
    public Result setRecommendArticle(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isRecommend = request.get("isRecommend");
            log.info("设置文章推荐请求: id={}, isRecommend={}", id, isRecommend);
            
            if (id == null || id <= 0) {
                return Result.fail("文章ID不能为空");
            }
            
            if (isRecommend == null) {
                return Result.fail("推荐状态不能为空");
            }
            
            boolean success = articleService.setRecommendArticle(id, isRecommend);
            if (success) {
                log.info("设置文章推荐成功: id={}, isRecommend={}", id, isRecommend);
                return Result.success(isRecommend ? "设置推荐成功" : "取消推荐成功");
            } else {
                return Result.fail("设置文章推荐失败");
            }
        } catch (Exception e) {
            log.error("设置文章推荐失败", e);
            return Result.fail("设置文章推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据分类获取文章列表
     */
    @GetMapping("/category/{categoryId}")
    public Result getArticlesByCategory(@PathVariable Integer categoryId) {
        try {
            log.info("根据分类获取文章请求: categoryId={}", categoryId);
            
            if (categoryId == null || categoryId <= 0) {
                return Result.fail("分类ID不能为空");
            }
            
            List<Article> articles = articleService.getArticlesByCategory(categoryId);
            
            log.info("根据分类获取文章成功: categoryId={}, count={}", categoryId, 
                    articles != null ? articles.size() : 0);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("根据分类获取文章失败", e);
            return Result.fail("根据分类获取文章失败");
        }
    }
    
    /**
     * 根据作者获取文章列表
     */
    @GetMapping("/author/{authorId}")
    public Result getArticlesByAuthor(@PathVariable Integer authorId) {
        try {
            log.info("根据作者获取文章请求: authorId={}", authorId);
            
            if (authorId == null || authorId <= 0) {
                return Result.fail("作者ID不能为空");
            }
            
            List<Article> articles = articleService.getArticlesByAuthor(authorId);
            
            log.info("根据作者获取文章成功: authorId={}, count={}", authorId, 
                    articles != null ? articles.size() : 0);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("根据作者获取文章失败", e);
            return Result.fail("根据作者获取文章失败");
        }
    }
    
    /**
     * 获取热门文章
     */
    @GetMapping("/hot")
    public Result getHotArticles(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("获取热门文章请求: limit={}", limit);
            
            List<Article> articles = articleService.getHotArticles(limit);
            
            log.info("获取热门文章成功: count={}", articles != null ? articles.size() : 0);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("获取热门文章失败", e);
            return Result.fail("获取热门文章失败");
        }
    }
    
    /**
     * 获取推荐文章
     */
    @GetMapping("/recommend")
    public Result getRecommendArticles(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("获取推荐文章请求: limit={}", limit);
            
            List<Article> articles = articleService.getRecommendArticles(limit);
            
            log.info("获取推荐文章成功: count={}", articles != null ? articles.size() : 0);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("获取推荐文章失败", e);
            return Result.fail("获取推荐文章失败");
        }
    }
    
    /**
     * 获取置顶文章
     */
    @GetMapping("/top")
    public Result getTopArticles() {
        try {
            log.info("获取置顶文章请求");
            
            List<Article> articles = articleService.getTopArticles();
            
            log.info("获取置顶文章成功: count={}", articles != null ? articles.size() : 0);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("获取置顶文章失败", e);
            return Result.fail("获取置顶文章失败");
        }
    }
    
    /**
     * 获取文章统计信息
     */
    @GetMapping("/statistics")
    public Result getArticleStatistics() {
        try {
            log.info("获取文章统计信息请求");
            
            Map<String, Object> statistics = articleService.getArticleStatistics();
            
            log.info("获取文章统计信息成功");
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取文章统计信息失败", e);
            return Result.fail("获取文章统计信息失败");
        }
    }
} 