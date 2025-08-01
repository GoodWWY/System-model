package com.wwy.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Article implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;                    // 文章ID
    private String title;                  // 文章标题
    private String content;                // 文章内容
    private String summary;                // 文章摘要
    private String coverImage;             // 封面图片
    private Integer authorId;              // 作者ID
    private String authorName;             // 作者名称
    private Integer categoryId;            // 分类ID
    private String categoryName;           // 分类名称
    private String tags;                   // 标签（逗号分隔）
    private String status;                 // 状态：draft(草稿)、published(已发布)、archived(已归档)
    private Integer viewCount;             // 阅读量
    private Integer likeCount;             // 点赞数
    private Integer commentCount;          // 评论数
    private Boolean isTop;                 // 是否置顶
    private Boolean isRecommend;           // 是否推荐
    private LocalDateTime publishTime;     // 发布时间
    private LocalDateTime createdAt;       // 创建时间
    private LocalDateTime updatedAt;       // 更新时间
    
    // 无参构造方法
    public Article() {}
    
    // 有参构造方法
    public Article(String title, String content, String summary, Integer authorId) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.authorId = authorId;
        this.status = "draft";
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.isTop = false;
        this.isRecommend = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 全参构造方法
    public Article(Integer id, String title, String content, String summary, String coverImage, 
                   Integer authorId, String authorName, Integer categoryId, String categoryName, 
                   String tags, String status, Integer viewCount, Integer likeCount, 
                   Integer commentCount, Boolean isTop, Boolean isRecommend, 
                   LocalDateTime publishTime, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.coverImage = coverImage;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tags = tags;
        this.status = status;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isTop = isTop;
        this.isRecommend = isRecommend;
        this.publishTime = publishTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter和Setter方法
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public Integer getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
    
    public Boolean getIsTop() {
        return isTop;
    }
    
    public void setIsTop(Boolean isTop) {
        this.isTop = isTop;
    }
    
    public Boolean getIsRecommend() {
        return isRecommend;
    }
    
    public void setIsRecommend(Boolean isRecommend) {
        this.isRecommend = isRecommend;
    }
    
    public LocalDateTime getPublishTime() {
        return publishTime;
    }
    
    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", summary='" + summary + '\'' +
                ", coverImage='" + coverImage + '\'' +
                ", authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", tags='" + tags + '\'' +
                ", status='" + status + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", isTop=" + isTop +
                ", isRecommend=" + isRecommend +
                ", publishTime=" + publishTime +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 