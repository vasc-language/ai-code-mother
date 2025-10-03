package com.spring.aicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送邮箱验证码
 * 接收请求参数类
 */
@Data
public class SendEmailCodeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码类型:REGISTER-注册, RESET_PASSWORD-重置密码
     */
    private String type;
}
