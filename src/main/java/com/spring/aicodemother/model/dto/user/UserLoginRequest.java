package com.spring.aicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录
 * 接收请求参数类
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 密码
     */
    private String userPassword;
}
