package com.spring.aicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * AI模型等级枚举
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Getter
public enum AiModelTierEnum {

    SIMPLE("简单", "SIMPLE", 1, "适合简单任务，速度快，成本低"),
    MEDIUM("中等", "MEDIUM", 3, "适合中等复杂度任务，性能均衡"),
    HARD("困难", "HARD", 8, "适合复杂任务，推理能力强"),
    EXPERT("专家", "EXPERT", 15, "适合最复杂任务，顶级性能");

    /**
     * 显示文本
     */
    private final String text;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 每1K token消耗的积分
     */
    private final Integer pointsPerKToken;

    /**
     * 等级描述
     */
    private final String description;

    AiModelTierEnum(String text, String value, Integer pointsPerKToken, String description) {
        this.text = text;
        this.value = value;
        this.pointsPerKToken = pointsPerKToken;
        this.description = description;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static AiModelTierEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AiModelTierEnum anEnum : AiModelTierEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
