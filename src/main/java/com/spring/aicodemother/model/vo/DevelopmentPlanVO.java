package com.spring.aicodemother.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 开发计划视图对象
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Schema(description = "开发计划")
public class DevelopmentPlanVO implements Serializable {

    @Schema(description = "计划ID（用于后续关联）")
    private String planId;

    @Schema(description = "计划内容（Markdown格式）")
    private String content;

    @Schema(description = "预估开发步骤数")
    private Integer estimatedSteps;

    @Schema(description = "预估文件数量")
    private Integer estimatedFiles;

    @Schema(description = "技术栈列表")
    private List<String> techStack;

    @Schema(description = "生成时间")
    private LocalDateTime createdAt;

    private static final long serialVersionUID = 1L;
}
