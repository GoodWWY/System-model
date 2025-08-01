package com.wwy.Mapper;

import com.wwy.Entity.QaRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 问答记录Mapper
 */
@Mapper
public interface QaRecordMapper {
    
    /**
     * 插入问答记录
     */
    int insert(QaRecord qaRecord);
    
    /**
     * 根据ID查询问答记录
     */
    QaRecord selectById(@Param("id") Long id);
    
    /**
     * 更新问答记录
     */
    int updateById(QaRecord qaRecord);
    
    /**
     * 分页查询问答记录
     */
    List<QaRecord> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize, 
                              @Param("userId") Integer userId, @Param("status") String status);
    
    /**
     * 统计问答记录总数
     */
    int countRecords(@Param("userId") Integer userId, @Param("status") String status);
    
    /**
     * 查询用户最近的问答记录
     */
    List<QaRecord> selectRecentByUserId(@Param("userId") Integer userId, @Param("limit") int limit);
    
    /**
     * 删除问答记录
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 批量删除问答记录
     */
    int batchDelete(@Param("ids") List<Long> ids);
    
    /**
     * 统计用户问题数量
     */
    int countUserQuestions(@Param("userId") Integer userId);
    
    /**
     * 获取热门问题
     */
    List<QaRecord> selectPopularQuestions(@Param("limit") int limit);
} 