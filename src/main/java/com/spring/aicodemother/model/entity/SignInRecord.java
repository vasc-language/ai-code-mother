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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到记录 实体类。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("sign_in_record")
public class SignInRecord implements Serializable {

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
     * 签到日期
     */
    @Column("signInDate")
    private LocalDate signInDate;

    /**
     * 连续签到天数
     */
    @Column("continuousDays")
    private Integer continuousDays;

    /**
     * 本次签到获得积分
     */
    @Column("pointsEarned")
    private Integer pointsEarned;

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
