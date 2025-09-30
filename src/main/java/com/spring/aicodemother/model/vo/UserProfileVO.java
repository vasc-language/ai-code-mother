package com.spring.aicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户主页信息视图对象
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
public class UserProfileVO implements Serializable {

    /**
     * 用户基本信息
     */
    private UserVO user;

    /**
     * 用户统计数据
     */
    private UserStatisticsVO statistics;

    private static final long serialVersionUID = 1L;
}