package com.spring.aicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用版本视图对象
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
public class AppVersionVO implements Serializable {

    /**
     * 版本记录id
     */
    private Long id;

    /**
     * 关联的应用id
     */
    private Long appId;

    /**
     * 版本号（1, 2, 3...）
     */
    private Integer versionNum;

    /**
     * 版本标签（v1, v2, v3...）
     */
    private String versionTag;

    /**
     * 代码内容（JSON格式，包含所有文件）
     */
    private String codeContent;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 部署URL
     */
    private String deployUrl;

    /**
     * 部署时间
     */
    private LocalDateTime deployedTime;

    /**
     * 部署操作用户id
     */
    private Long userId;

    /**
     * 版本备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 部署操作用户信息
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;
}
