package com.spring.aicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮箱验证码 实体类
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("email_verification_code")
public class EmailVerificationCode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 邮箱地址
     */
    @Column("email")
    private String email;

    /**
     * 验证码
     */
    @Column("code")
    private String code;

    /**
     * 验证码类型:REGISTER-注册, RESET_PASSWORD-重置密码, LOGIN-登录
     */
    @Column("type")
    private String type;

    /**
     * 过期时间
     */
    @Column("expireTime")
    private LocalDateTime expireTime;

    /**
     * 是否已使用:0-未使用, 1-已使用
     */
    @Column("verified")
    private Integer verified;

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
     * 逻辑删除:0-未删除,1-已删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}
