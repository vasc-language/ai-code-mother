package com.spring.aicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息更新请求（用于个人主页编辑）
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
public class UserProfileUpdateRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}