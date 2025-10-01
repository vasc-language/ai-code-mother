package com.spring.aicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用版本历史 实体类。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_version")
public class AppVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 版本记录id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 关联的应用id
     */
    @Column("appId")
    private Long appId;

    /**
     * 版本号（1, 2, 3...）
     */
    @Column("versionNum")
    private Integer versionNum;

    /**
     * 版本标签（v1, v2, v3...）
     */
    @Column("versionTag")
    private String versionTag;

    /**
     * 代码内容（JSON格式，包含所有文件）
     */
    @Column("codeContent")
    private String codeContent;

    /**
     * 部署标识
     */
    @Column("deployKey")
    private String deployKey;

    /**
     * 部署URL
     */
    @Column("deployUrl")
    private String deployUrl;

    /**
     * 部署时间
     */
    @Column("deployedTime")
    private LocalDateTime deployedTime;

    /**
     * 部署操作用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 版本备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
