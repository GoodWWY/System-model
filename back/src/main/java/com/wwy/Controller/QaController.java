package com.wwy.Controller;

import com.wwy.Common.ActivityLogger;
import com.wwy.Common.Result;
import com.wwy.Entity.QaRecord;
import com.wwy.Entity.Dto.QaRequest;
import com.wwy.Service.IQaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 问答控制器
 */
@Slf4j
@RestController
@RequestMapping("/qa")
public class QaController {
    
    @Autowired
    private IQaService qaService;
    
    @Autowired
    private ActivityLogger activityLogger;
    
    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result test() {
        return Result.success("QA服务正常运行");
    }
    
    /**
     * 提交问题
     */
    @PostMapping("/ask")
    public Result askQuestion(@RequestBody QaRequest qaRequest, HttpServletRequest request) {
        try {
            // 简化用户信息获取逻辑
            String username = request.getHeader("username");
            String userIdStr = request.getHeader("userId");
            
            // 如果请求头没有用户信息，使用默认值
            if (username == null || username.trim().isEmpty()) {
                username = "admin";
            }
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                userIdStr = "1";
            }
            
            Integer userId;
            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                userId = 1; // 默认用户ID
            }

            // 基本验证
            if (qaRequest.getQuestion() == null || qaRequest.getQuestion().trim().isEmpty()) {
                return Result.fail("问题不能为空");
            }
            
            if (qaRequest.getQuestion().length() > 1000) {
                return Result.fail("问题长度不能超过1000字符");
            }
            
            log.info("用户[{}]提交问题: {}", username, qaRequest.getQuestion());
            
            // 提交问题并获取回答
            QaRecord qaRecord = qaService.askQuestion(userId, username, qaRequest);
            
            // 记录用户活动
            if (qaRecord.getStatus().equals(QaRecord.STATUS_COMPLETED)) {
                activityLogger.log(userId, username, "ASK_QUESTION", 
                        "提问: " + truncateText(qaRequest.getQuestion(), 50));
            }
            
            return Result.success("问题处理完成", qaRecord);
            
        } catch (Exception e) {
            log.error("处理问题时发生异常: {}", e.getMessage(), e);
            return Result.fail("处理问题时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取问答记录列表
     */
    @GetMapping("/records")
    public Result getRecords(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(required = false) Integer userId,
                           @RequestParam(required = false) String status) {
        try {
            Map<String, Object> result = qaService.getQaRecords(pageNum, pageSize, userId, status);
            return Result.success("获取问答记录成功", result);
        } catch (Exception e) {
            log.error("获取问答记录失败: {}", e.getMessage(), e);
            return Result.fail("获取问答记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户最近的问答记录
     */
    @GetMapping("/recent")
    public Result getRecentRecords(@RequestParam Integer userId,
                                 @RequestParam(defaultValue = "10") int limit) {
        try {
            List<QaRecord> records = qaService.getUserRecentRecords(userId, limit);
            return Result.success("获取最近问答记录成功", records);
        } catch (Exception e) {
            log.error("获取最近问答记录失败: {}", e.getMessage(), e);
            return Result.fail("获取最近问答记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取问答记录详情
     */
    @GetMapping("/record/{id}")
    public Result getRecordById(@PathVariable Long id) {
        try {
            QaRecord record = qaService.getQaRecordById(id);
            if (record != null) {
                return Result.success("获取问答记录成功", record);
            } else {
                return Result.fail("问答记录不存在");
            }
        } catch (Exception e) {
            log.error("获取问答记录详情失败: {}", e.getMessage(), e);
            return Result.fail("获取问答记录详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除问答记录
     */
    @DeleteMapping("/record/{id}")
    public Result deleteRecord(@PathVariable Long id, 
                             @RequestParam Integer userId) {
        try {
            boolean success = qaService.deleteQaRecord(id, userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.fail("删除失败，记录不存在或无权限");
            }
        } catch (Exception e) {
            log.error("删除问答记录失败: {}", e.getMessage(), e);
            return Result.fail("删除问答记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除问答记录
     */
    @PostMapping("/batch-delete")
    public Result batchDeleteRecords(@RequestBody List<Long> ids,
                                   @RequestParam Integer userId) {
        try {
            boolean success = qaService.batchDeleteQaRecords(ids, userId);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.fail("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除问答记录失败: {}", e.getMessage(), e);
            return Result.fail("批量删除问答记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 对问答记录进行评分
     */
    @PostMapping("/rate")
    public Result rateRecord(@RequestParam Long id,
                           @RequestParam Integer userId,
                           @RequestParam Integer rating,
                           @RequestParam(required = false) String feedback) {
        try {
            boolean success = qaService.rateQaRecord(id, userId, rating, feedback);
            if (success) {
                return Result.success("评分成功");
            } else {
                return Result.fail("评分失败，记录不存在或无权限");
            }
        } catch (Exception e) {
            log.error("评分问答记录失败: {}", e.getMessage(), e);
            return Result.fail("评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取问答统计信息
     */
    @GetMapping("/statistics")
    public Result getStatistics(@RequestParam(required = false) Integer userId) {
        try {
            Map<String, Object> statistics = qaService.getQaStatistics(userId);
            return Result.success("获取统计信息成功", statistics);
        } catch (Exception e) {
            log.error("获取问答统计信息失败: {}", e.getMessage(), e);
            return Result.fail("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门问题
     */
    @GetMapping("/popular")
    public Result getPopularQuestions(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<QaRecord> questions = qaService.getPopularQuestions(limit);
            return Result.success("获取热门问题成功", questions);
        } catch (Exception e) {
            log.error("获取热门问题失败: {}", e.getMessage(), e);
            return Result.fail("获取热门问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 截断文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
} 