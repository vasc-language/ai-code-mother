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
 * 积分明细 实体类。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("points_record")
public class PointsRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 积分变动数量（正数为增加，负数为扣减）
     */
    @Column("points")
    private Integer points;

    /**
     * 变动后余额
     */
    @Column("balance")
    private Integer balance;

    /**
     * 积分类型（SIGN_IN:签到, REGISTER:注册, INVITE:邀请, GENERATE:生成应用, EXPIRE:过期）
     */
    @Column("type")
    private String type;

    /**
     * 变动原因描述
     */
    @Column("reason")
    private String reason;

    /**
     * 关联ID（如应用ID、邀请记录ID）
     */
    @Column("relatedId")
    private Long relatedId;

    /**
     * 使用的AI模型key（用于AI生成积分扣除）
     */
    @Column("model_key")
    private String modelKey;

    /**
     * 消耗的token数量（用于AI生成积分扣除）
     */
    @Column("token_count")
    private Integer tokenCount;

    /**
     * 积分过期时间
     */
    @Column("expireTime")
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
