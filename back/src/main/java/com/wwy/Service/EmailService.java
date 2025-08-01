package com.wwy.Service;

import com.wwy.Common.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisUtil redisUtil;

    // 验证码过期时间（秒）
    private static final int CODE_EXPIRE_TIME = 300; // 5分钟

    /**
     * 发送注册验证码邮件
     */
    public boolean sendRegisterVerificationCode(String email) {
        try {
            // 生成6位数字验证码
            String verificationCode = generateVerificationCode();
            
            // 存储验证码到Redis，设置5分钟过期
            String redisKey = "register_code:" + email;
            redisUtil.set(redisKey, verificationCode, CODE_EXPIRE_TIME);
            
            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("1781939957@qq.com");
            message.setTo(email);
            message.setSubject("【Cursor Demo】注册验证码");
            message.setText(buildRegisterEmailContent(verificationCode));
            
            logger.info("准备发送邮件，发件人: {}, 收件人: {}", message.getFrom(), email);
            mailSender.send(message);
            logger.info("注册验证码邮件发送成功，邮箱: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("发送注册验证码邮件失败，邮箱: {}，错误详情: {}", email, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证注册验证码
     */
    public boolean verifyRegisterCode(String email, String code) {
        try {
            String redisKey = "register_code:" + email;
            String storedCode = (String) redisUtil.get(redisKey);
            
            if (storedCode == null) {
                logger.warn("验证码已过期或不存在，邮箱: {}", email);
                return false;
            }
            
            boolean isValid = storedCode.equals(code);
            if (isValid) {
                // 验证成功后删除验证码
                redisUtil.del(redisKey);
                logger.info("注册验证码验证成功，邮箱: {}", email);
            } else {
                logger.warn("注册验证码验证失败，邮箱: {}，输入码: {}，存储码: {}", email, code, storedCode);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("验证注册验证码失败，邮箱: {}", email, e);
            return false;
        }
    }

    /**
     * 检查验证码发送频率限制
     */
    public boolean checkSendFrequency(String email) {
        try {
            String frequencyKey = "send_frequency:" + email;
            String lastSendTime = (String) redisUtil.get(frequencyKey);
            
            if (lastSendTime != null) {
                long currentTime = System.currentTimeMillis();
                long lastTime = Long.parseLong(lastSendTime);
                
                // 限制60秒内只能发送一次
                if (currentTime - lastTime < 60000) {
                    logger.warn("发送验证码过于频繁，邮箱: {}", email);
                    return false;
                }
            }
            
            // 记录本次发送时间
            redisUtil.set(frequencyKey, String.valueOf(System.currentTimeMillis()), 60);
            return true;
            
        } catch (Exception e) {
            logger.error("检查发送频率失败，邮箱: {}", email, e);
            return true; // 出错时允许发送
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 构建注册邮件内容
     */
    private String buildRegisterEmailContent(String verificationCode) {
        return "尊敬的用户：\n\n" +
               "您正在注册 Cursor Demo 账号，验证码为：\n\n" +
               "【" + verificationCode + "】\n\n" +
               "验证码有效期为5分钟，请及时使用。\n" +
               "如果这不是您的操作，请忽略此邮件。\n\n" +
               "祝您使用愉快！\n" +
               "Cursor Demo 团队";
    }

    /**
     * 发送找回密码验证码邮件
     */
    public boolean sendResetPasswordCode(String email) {
        try {
            // 生成6位数字验证码
            String verificationCode = generateVerificationCode();
            
            // 存储验证码到Redis，设置5分钟过期
            String redisKey = "reset_password_code:" + email;
            redisUtil.set(redisKey, verificationCode, CODE_EXPIRE_TIME);
            
            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("1781939957@qq.com");
            message.setTo(email);
            message.setSubject("【Cursor Demo】密码重置验证码");
            message.setText(buildResetPasswordEmailContent(verificationCode));
            
            mailSender.send(message);
            logger.info("密码重置验证码邮件发送成功，邮箱: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("发送密码重置验证码邮件失败，邮箱: {}", email, e);
            return false;
        }
    }

    /**
     * 验证密码重置验证码
     */
    public boolean verifyResetPasswordCode(String email, String code) {
        try {
            String redisKey = "reset_password_code:" + email;
            String storedCode = (String) redisUtil.get(redisKey);
            
            if (storedCode == null) {
                logger.warn("密码重置验证码已过期或不存在，邮箱: {}", email);
                return false;
            }
            
            boolean isValid = storedCode.equals(code);
            if (isValid) {
                // 验证成功后删除验证码
                redisUtil.del(redisKey);
                logger.info("密码重置验证码验证成功，邮箱: {}", email);
            } else {
                logger.warn("密码重置验证码验证失败，邮箱: {}", email);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("验证密码重置验证码失败，邮箱: {}", email, e);
            return false;
        }
    }

    /**
     * 构建密码重置邮件内容
     */
    private String buildResetPasswordEmailContent(String verificationCode) {
        return "尊敬的用户：\n\n" +
               "您正在重置 Cursor Demo 账号密码，验证码为：\n\n" +
               "【" + verificationCode + "】\n\n" +
               "验证码有效期为5分钟，请及时使用。\n" +
               "如果这不是您的操作，请立即检查账号安全。\n\n" +
               "祝您使用愉快！\n" +
               "Cursor Demo 团队";
    }
} 