package com.spring.aicodemother.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.mapper.EmailVerificationCodeMapper;
import com.spring.aicodemother.model.entity.EmailVerificationCode;
import com.spring.aicodemother.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 邮件服务实现类
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Service
public class EmailServiceImpl extends ServiceImpl<EmailVerificationCodeMapper, EmailVerificationCode> implements EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 验证码有效期(分钟)
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 发送频率限制(秒)
     */
    private static final int SEND_INTERVAL_SECONDS = 60;

    @Override
    public boolean sendVerificationCode(String email, String type) {
        // 1. 校验参数
        if (StrUtil.hasBlank(email, type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 检查发送频率(60秒内不能重复发送)
        LocalDateTime limitTime = LocalDateTime.now().minusSeconds(SEND_INTERVAL_SECONDS);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("email", email)
                .eq("type", type)
                .ge("createTime", limitTime)
                .orderBy("createTime", false);

        EmailVerificationCode recentCode = this.mapper.selectOneByQuery(queryWrapper);
        if (recentCode != null) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FREQUENT);
        }

        // 3. 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 保存到数据库
        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES))
                .verified(0)
                .build();
        save(verificationCode);

        // 5. 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【AICodeHub 官网】验证码");
            message.setText(buildEmailContent(code, type));

            mailSender.send(message);
            log.info("验证码邮件发送成功: email={}, type={}", email, type);
            return true;
        } catch (Exception e) {
            log.error("验证码邮件发送失败: email={}, type={}", email, type, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_ERROR, e.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String email, String code, String type) {
        // 1. 校验参数
        if (StrUtil.hasBlank(email, code, type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 查询验证码
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("email", email)
                .eq("code", code)
                .eq("type", type)
                .eq("verified", 0)
                .ge("expireTime", LocalDateTime.now())
                .orderBy("createTime", false);

        EmailVerificationCode verificationCode = this.mapper.selectOneByQuery(queryWrapper);

        // 3. 验证码不存在或已过期
        if (verificationCode == null) {
            return false;
        }

        // 4. 标记为已使用
        verificationCode.setVerified(1);
        updateById(verificationCode);

        log.info("验证码验证成功: email={}, type={}", email, type);
        return true;
    }

    @Override
    public int cleanExpiredCodes() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .lt("expireTime", LocalDateTime.now());

        int count = this.mapper.deleteByQuery(queryWrapper);
        log.info("清理过期验证码: count={}", count);
        return count;
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, String type) {
        String purpose = switch (type) {
            case "REGISTER" -> "注册账号";
            case "RESET_PASSWORD" -> "重置密码";
            case "LOGIN" -> "登录验证";
            default -> "身份验证";
        };

        return String.format("""
                        尊敬的用户你好 ( ^_^ )ノ

                        感谢你信任 AICodeHub！
                        当前正在进行的操作类型是:【%s】

                        你的验证码为:%s

                        验证码有效期为%d分钟,请妥善保管,不要分享给他人。

                        想进一步了解我们,欢迎访问:

                        官网: https://www.joinoai.cloud/

                        GitHub 项目: https://github.com/vasc-language/ai-code-mother

                        如非本人操作,请忽略此邮件,我们一直在守护你的安全 \\(^_^)/

                        ---
                        AICodeHub 团队
                        """,
                purpose, code, CODE_EXPIRE_MINUTES);
    }
}
