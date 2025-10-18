package com.spring.aicodemother.model.dto.plan;

import com.spring.aicodemother.model.vo.DevelopmentPlanVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 计划缓存数据
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
public class PlanCacheData implements Serializable {

    private Long appId;

    private Long userId;

    private String requirement;

    private DevelopmentPlanVO plan;

    private LocalDateTime createdAt;

    private static final long serialVersionUID = 1L;
}
