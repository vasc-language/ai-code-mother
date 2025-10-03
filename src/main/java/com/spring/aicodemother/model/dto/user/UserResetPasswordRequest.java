package com.spring.aicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户重置密码请求
 * 接收请求参数类
 */
@Data
public class UserResetPasswordRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 邮箱验证码
     */
    private String emailCode;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
