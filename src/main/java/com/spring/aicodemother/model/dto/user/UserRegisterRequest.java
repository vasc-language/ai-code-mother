package com.spring.aicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册
 * 接收请求参数类
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 邮箱验证码
     */
    private String emailCode;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 邀请码(可选)
     */
    private String inviteCode;
}
