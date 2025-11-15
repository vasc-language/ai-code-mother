package com.spring.aicodemother.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 需要在监听器中获取业务维度的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {

    private String userId;

    private String appId;

    /**
     * 选择的AI模型key
     */
    private String modelKey;

    /**
     * 最近一次生成的Token总消耗
     */
    private Long totalTokens;

    /**
     * 积分扣费是否失败（用于异常处理）
     */
    private Boolean pointsDeductionFailed;

    /**
     * 积分扣费失败原因
     */
    private String pointsDeductionFailureReason;

    @Serial
    private static final long serialVersionUID = 1L;
}
