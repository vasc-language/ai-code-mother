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
 * 邀请关系 实体类。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("invite_record")
public class InviteRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 邀请人ID
     */
    @Column("inviterId")
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    @Column("inviteeId")
    private Long inviteeId;

    /**
     * 邀请码
     */
    @Column("inviteCode")
    private String inviteCode;

    /**
     * 注册IP
     */
    @Column("registerIp")
    private String registerIp;

    /**
     * 设备ID
     */
    @Column("deviceId")
    private String deviceId;

    /**
     * 状态（PENDING:待确认, REGISTERED:已注册, REWARDED:已奖励）
     */
    @Column("status")
    private String status;

    /**
     * 邀请人获得积分
     */
    @Column("inviterPoints")
    private Integer inviterPoints;

    /**
     * 被邀请人获得积分
     */
    @Column("inviteePoints")
    private Integer inviteePoints;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 注册时间
     */
    @Column("registerTime")
    private LocalDateTime registerTime;

    /**
     * 奖励发放时间
     */
    @Column("rewardTime")
    private LocalDateTime rewardTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
