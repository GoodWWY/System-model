package com.wwy.Controller;

import com.wwy.Common.Result;
import com.wwy.Common.AliyunOSSUtil;
import com.wwy.Common.ActivityLogger;
import com.wwy.Entity.Dto.LoginRequest;
import com.wwy.Entity.Dto.RegisterRequest;
import com.wwy.Entity.Role;
import com.wwy.Entity.User;
import com.wwy.Service.EmailService;
import com.wwy.Service.IRoleService;
import com.wwy.Service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestParam;
import com.wwy.Common.BCryptUtil;
import com.wwy.Common.JwtUtil;


// 添加HTTP响应相关导入
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/user")
public class UserController {

   private static final Logger log = LoggerFactory.getLogger(UserController.class);

   @Autowired
   private IUserService userService;
   
   @Autowired
   private IRoleService roleService;
   
   @Autowired
   private AliyunOSSUtil aliyunOSSUtil;
   
   @Autowired
   private EmailService emailService;
   
   @Autowired
   private ActivityLogger activityLogger;

   @PostMapping("/login")
   public Result login(@RequestBody LoginRequest loginRequest){
      String username = loginRequest.getUsername();
      String password = loginRequest.getPassword();

      User user = userService.login(username, password);
      if(user != null){
         String token = JwtUtil.generateToken(username);
         
         // 记录登录活动
         activityLogger.logLogin(user.getUid(), user.getUsername());
         
         // 返回用户基本信息和token
         Map<String, Object> data = new HashMap<>();
         data.put("token", token);
         data.put("uid", user.getUid());
         data.put("username", user.getUsername());
         
         return Result.success(data);
      }
      else return Result.fail("用户名或密码错误");
}

   /**
    * 发送注册验证码
    */
   @PostMapping("/send-register-code")
   public Result sendRegisterCode(@RequestParam String email) {
      try {
         // 基本参数验证
         if (email == null || email.trim().isEmpty()) {
            return Result.fail("邮箱不能为空");
         }
         
         // 邮箱格式验证
         if (!isValidEmail(email)) {
            return Result.fail("邮箱格式不正确");
         }
         
         // 检查邮箱是否已被注册
         if (userService.isEmailExists(email)) {
            return Result.fail("该邮箱已被注册");
         }
         
         // 检查发送频率
         if (!emailService.checkSendFrequency(email)) {
            return Result.fail("发送过于频繁，请60秒后再试");
         }
         
         // 发送验证码
         boolean success = emailService.sendRegisterVerificationCode(email);
         if (success) {
            return Result.success("验证码发送成功，请查收邮件");
         } else {
            return Result.fail("验证码发送失败，请稍后重试");
         }
         
      } catch (Exception e) {
         log.error("发送注册验证码失败", e);
         return Result.fail("系统异常，请联系管理员");
      }
   }

   /**
    * 用户注册（需验证邮箱验证码）
    */
   @PostMapping("/register")
   public Result register(@RequestBody RegisterRequest registerRequest){
      try {
         String username = registerRequest.getUsername();
         String password = registerRequest.getPassword();
         String email = registerRequest.getEmail();
         String verificationCode = registerRequest.getVerificationCode();
         
         // 基本参数验证
         if (username == null || username.trim().isEmpty() || 
             password == null || password.trim().isEmpty() ||
             email == null || email.trim().isEmpty() ||
             verificationCode == null || verificationCode.trim().isEmpty()) {
            return Result.fail("所有字段都是必填的");
         }
         
         // 邮箱格式验证
         if (!isValidEmail(email)) {
            return Result.fail("邮箱格式不正确");
         }
         
         // 验证邮箱验证码
         if (!emailService.verifyRegisterCode(email, verificationCode)) {
            return Result.fail("验证码无效或已过期");
         }
         
         // 注册用户
         boolean success = userService.register(username, password, email);
         if (success) {
            // 获取新注册的用户信息来记录活动
                         User newUser = userService.getUserByUsername(username);
            if (newUser != null) {
               activityLogger.logRegister(newUser.getUid(), newUser.getUsername());
            }
            return Result.success("注册成功，请登录");
         } else {
            return Result.fail("用户名已存在或注册失败");
         }
         
      } catch (Exception e) {
         log.error("用户注册失败", e);
         return Result.fail("系统异常，请联系管理员");
      }
   }
   
   /**
    * 邮箱格式验证
    */
   private boolean isValidEmail(String email) {
      String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
      return email.matches(emailRegex);
   }

   @GetMapping("/list")
   public Result listUsers(@RequestParam(defaultValue = "1") int pageNum, 
                         @RequestParam(defaultValue = "10") int pageSize) {
      try {
         List<User> users = userService.listUsersWithRoles(pageNum, pageSize);
         int total = userService.countUsers();
         
         Map<String, Object> data = new HashMap<>();
         data.put("records", users);
         data.put("total", total);
         data.put("pageNum", pageNum);
         data.put("pageSize", pageSize);
         
         return Result.success(data);
      } catch (Exception e) {
         log.error("获取用户列表失败", e);
         return Result.fail("获取用户列表失败");
      }
   }

   @GetMapping("/search")
   public Result searchUsers(@RequestParam(required = false) String username,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) Integer roleId,
                           @RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize) {
      try {
         // 构建搜索参数
         Map<String, Object> params = new HashMap<>();
         if (username != null && !username.trim().isEmpty()) {
            params.put("username", username.trim());
         }
         if (email != null && !email.trim().isEmpty()) {
            params.put("email", email.trim());
         }
         if (status != null && !status.trim().isEmpty()) {
            params.put("status", status.trim());
         }
         if (roleId != null) {
            params.put("roleId", roleId);
         }
         params.put("pageNum", pageNum);
         params.put("pageSize", pageSize);
         
         List<User> users = userService.searchUsersWithRoles(params);
         int total = userService.countSearchUsers(params);
         
         Map<String, Object> data = new HashMap<>();
         data.put("records", users);
         data.put("total", total);
         data.put("pageNum", pageNum);
         data.put("pageSize", pageSize);
         
         return Result.success(data);
      } catch (Exception e) {
         log.error("搜索用户失败", e);
         return Result.fail("搜索用户失败");
      }
   }

   @PostMapping("/update")
   public Result updateUser(@RequestBody User user) {
      try {
         // 基本参数验证
         if (user.getUid() == null) {
            return Result.fail("用户ID不能为空");
         }
         
         // 如果包含密码，需要加密
         if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            user.setPassword(BCryptUtil.encode(user.getPassword()));
         }
         
         boolean success = userService.updateUser(user);
         if (success) {
            return Result.success("用户信息更新成功");
         } else {
            return Result.fail("用户信息更新失败");
         }
      } catch (Exception e) {
         log.error("更新用户信息失败", e);
         return Result.fail("更新用户信息失败");
      }
   }

   @DeleteMapping("/delete/{uid}")
   public Result deleteUser(@PathVariable Integer uid) {
      try {
         boolean success = userService.deleteUser(uid);
         if (success) {
            return Result.success("用户删除成功");
         } else {
            return Result.fail("用户删除失败，用户可能不存在");
         }
      } catch (Exception e) {
         log.error("删除用户失败", e);
         return Result.fail("删除用户失败");
      }
   }

   /**
    * 批量删除用户
    */
   @DeleteMapping("/batch")
   public Result batchDeleteUsers(@RequestBody List<Integer> uids) {
      try {
         log.info("批量删除用户请求: uids={}", uids);
         
         if (uids == null || uids.isEmpty()) {
            return Result.fail("用户ID列表不能为空");
         }
         
         boolean success = userService.batchDeleteUsers(uids);
         if (success) {
            log.info("批量删除用户成功: count={}", uids.size());
            return Result.success("批量删除成功");
         } else {
            return Result.fail("批量删除用户失败");
         }
      } catch (Exception e) {
         log.error("批量删除用户失败", e);
         return Result.fail("批量删除用户失败");
      }
   }

   /**
    * 批量删除用户 (POST方式)
    */
   @PostMapping("/batch-delete")
   public Result batchDeleteUsersPost(@RequestBody List<Integer> uids) {
      try {
         log.info("批量删除用户请求(POST): uids={}", uids);
         
         if (uids == null || uids.isEmpty()) {
            return Result.fail("用户ID列表不能为空");
         }
         
         boolean success = userService.batchDeleteUsers(uids);
         if (success) {
            log.info("批量删除用户成功(POST): count={}", uids.size());
            return Result.success("批量删除成功");
         } else {
            return Result.fail("批量删除用户失败");
         }
      } catch (Exception e) {
         log.error("批量删除用户失败(POST)", e);
         return Result.fail("批量删除用户失败");
      }
   }

   @PostMapping("/upload-avatar")
   public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
      if (file.isEmpty()) {
         return Result.fail("文件不能为空");
      }
      // 检查文件大小（限制为10MB）
      if (file.getSize() > 10 * 1024 * 1024) {
         return Result.fail("文件大小不能超过10MB");
      }
      // 检查文件类型
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
         return Result.fail("只能上传图片文件");
      }
      
      try {
         log.info("开始上传头像到OSS, 文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
         
         // 使用阿里云OSS上传文件
         String avatarUrl = aliyunOSSUtil.uploadFile(file, "avatars");
         
         log.info("头像上传OSS成功: {}", avatarUrl);
         return Result.success(avatarUrl);
         
      } catch (Exception e) {
         log.error("头像上传OSS失败: {}", e.getMessage(), e);
         return Result.fail("头像上传失败: " + e.getMessage());
      }
   }

   @GetMapping("/profile")
   public Result getUserProfile(@RequestParam Integer uid) {
      try {
         User user = userService.getUserById(uid);
         if (user != null) {
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
         } else {
            return Result.fail("用户不存在");
         }
      } catch (Exception e) {
         log.error("获取用户资料失败", e);
         return Result.fail("获取用户资料失败");
      }
   }

   @PostMapping("/profile")
   public Result updateUserProfile(@RequestBody User user) {
      try {
         // 基本参数验证
         if (user.getUid() == null) {
            return Result.fail("用户ID不能为空");
         }
         
         // 获取当前用户信息用于记录活动
         User currentUser = userService.getUserById(user.getUid());
         if (currentUser == null) {
            return Result.fail("用户不存在");
         }
         
         // 清除密码字段，避免意外更新
         user.setPassword(null);
         
         boolean success = userService.updateUser(user);
         if (success) {
            // 检查是否更新了头像
            if (user.getAvatar() != null && !user.getAvatar().equals(currentUser.getAvatar())) {
               activityLogger.logUploadAvatar(user.getUid(), currentUser.getUsername());
            } else {
               // 记录更新个人信息活动
               activityLogger.logUpdateProfile(user.getUid(), currentUser.getUsername());
            }
            return Result.success("个人资料更新成功");
         } else {
            return Result.fail("个人资料更新失败");
         }
      } catch (Exception e) {
         log.error("更新个人资料失败", e);
         return Result.fail("更新个人资料失败");
      }
   }

   @GetMapping("/export")
   public ResponseEntity<?> exportUsers(@RequestParam(required = false) String username,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) Integer roleId) {
      try {
         // 构建搜索参数
         Map<String, Object> params = new HashMap<>();
         if (username != null && !username.trim().isEmpty()) {
            params.put("username", username.trim());
         }
         if (email != null && !email.trim().isEmpty()) {
            params.put("email", email.trim());
         }
         if (status != null && !status.trim().isEmpty()) {
            params.put("status", status.trim());
         }
         if (roleId != null) {
            params.put("roleId", roleId);
         }
         
         byte[] excelBytes = userService.exportUsersToExcel(params);
         
         if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(Result.fail("导出文件为空"));
         }
         
         // 生成文件名
         String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
         String filename = "用户列表_" + timestamp + ".xlsx";
         
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
         headers.setContentDispositionFormData("attachment", java.net.URLEncoder.encode(filename, "UTF-8"));
         
         return ResponseEntity.ok()
               .headers(headers)
               .body(excelBytes);
               
      } catch (Exception e) {
         log.error("导出用户列表失败", e);
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .contentType(MediaType.APPLICATION_JSON)
               .body(Result.fail("导出失败：" + e.getMessage()));
      }
   }

   @PostMapping("/change-password")
   public Result changePassword(@RequestBody Map<String, String> params) {
      try {
         Integer uid = Integer.valueOf(params.get("uid"));
         String oldPassword = params.get("oldPassword");
         String newPassword = params.get("newPassword");
         
         // 参数验证
         if (uid == null || oldPassword == null || newPassword == null) {
            return Result.fail("参数不能为空");
         }
         
         if (newPassword.length() < 6) {
            return Result.fail("新密码长度不能少于6位");
         }
         
         // 获取用户信息
         User user = userService.getUserById(uid);
         if (user == null) {
            return Result.fail("用户不存在");
         }
         
         // 验证旧密码
         if (!BCryptUtil.matches(oldPassword, user.getPassword())) {
            return Result.fail("原密码不正确");
         }
         
         // 更新密码
         user.setPassword(BCryptUtil.encode(newPassword));
         boolean success = userService.updateUser(user);
         
         if (success) {
            return Result.success("密码修改成功");
         } else {
            return Result.fail("密码修改失败");
         }
         
      } catch (Exception e) {
         log.error("修改密码失败", e);
         return Result.fail("修改密码失败");
      }
   }

   /**
    * 获取用户角色信息
    */
   @GetMapping("/roles")
   public Result getUserRoles(@RequestParam Integer uid) {
      try {
         log.info("获取用户角色请求: uid={}", uid);
         
         if (uid == null) {
            return Result.fail("用户ID不能为空");
         }
         
         List<Role> roles = roleService.getRolesByUserId(uid);
         if (roles != null) {
            log.info("获取用户角色成功: uid={}, roles={}", uid, roles.size());
            return Result.success(roles);
         } else {
            return Result.success(List.of()); // 返回空列表而不是失败
         }
      } catch (Exception e) {
         log.error("获取用户角色失败: uid={}", uid, e);
         return Result.fail("获取用户角色失败");
      }
   }

   /**
    * 更新用户状态
    */
   @PutMapping("/status")
   public Result updateUserStatus(@RequestParam Integer uid, @RequestParam String status) {
      try {
         log.info("更新用户状态请求: uid={}, status={}", uid, status);
         
         if (uid == null || status == null || status.trim().isEmpty()) {
            return Result.fail("用户ID和状态不能为空");
         }
         
         // 验证状态值
         if (!status.equals("active") && !status.equals("inactive")) {
            return Result.fail("状态值必须为 active 或 inactive");
         }
         
         User user = userService.getUserById(uid);
         if (user == null) {
            return Result.fail("用户不存在");
         }
         
         user.setStatus(status);
         boolean success = userService.updateUser(user);
         if (success) {
            log.info("更新用户状态成功: uid={}, status={}", uid, status);
            return Result.success("用户状态更新成功");
         } else {
            return Result.fail("用户状态更新失败");
         }
      } catch (Exception e) {
         log.error("更新用户状态失败: uid={}, status={}", uid, status, e);
         return Result.fail("更新用户状态失败");
      }
   }

   /**
    * 更新用户资料 (PUT方式)
    */
   @PutMapping("/profile")
   public Result updateUserProfilePut(@RequestBody User user, @RequestParam(required = false) Integer uid) {
      try {
         // 如果URL参数中有uid，使用URL参数的uid
         if (uid != null) {
            user.setUid(uid);
         }
         
         log.info("更新用户资料请求(PUT): uid={}", user.getUid());
         
         // 基本参数验证
         if (user.getUid() == null) {
            return Result.fail("用户ID不能为空");
         }
         
         // 清除密码字段，避免意外更新
         user.setPassword(null);
         
         boolean success = userService.updateUser(user);
         if (success) {
            log.info("更新用户资料成功(PUT): uid={}", user.getUid());
            return Result.success("用户资料更新成功");
         } else {
            return Result.fail("用户资料更新失败");
         }
      } catch (Exception e) {
         log.error("更新用户资料失败(PUT): uid={}", user.getUid(), e);
         return Result.fail("更新用户资料失败");
      }
   }
}



