package com.spring.aicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户统计数据视图对象
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
public class UserStatisticsVO implements Serializable {

    /**
     * 创建应用数
     */
    private Long appCount;

    /**
     * 生成次数（暂时使用应用更新次数作为代理指标）
     */
    private Long generateCount;

    /**
     * 累计使用天数
     */
    private Long joinDays;

    /**
     * 最近活跃时间
     */
    private LocalDateTime lastActiveTime;

    private static final long serialVersionUID = 1L;
}