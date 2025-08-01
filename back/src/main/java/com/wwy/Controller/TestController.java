package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Service.QwenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private QwenAiService qwenAiService;
    
    @GetMapping("/hello")
    public Result hello() {
        return Result.success("Hello World!");
    }
    
    @GetMapping("/qa")
    public Result qaTest() {
        return Result.success("QA服务测试成功");
    }
    
    @PostMapping("/qwen")
    public Result testQwen(@RequestParam String question) {
        try {
            QwenAiService.QwenResponse response = qwenAiService.askQuestion(
                question, "qwen-turbo", 500, 0.7);
            
            if (response.isSuccess()) {
                return Result.success("通义千问API测试成功", response);
            } else {
                return Result.fail("通义千问API测试失败: " + response.getErrorMessage());
            }
        } catch (Exception e) {
            return Result.fail("测试异常: " + e.getMessage());
        }
    }
} 