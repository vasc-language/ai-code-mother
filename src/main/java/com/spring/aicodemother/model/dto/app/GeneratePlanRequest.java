package com.spring.aicodemother.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 生成开发计划请求
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Schema(description = "生成开发计划请求")
public class GeneratePlanRequest implements Serializable {

    @Schema(description = "用户需求描述")
    private String message;

    private static final long serialVersionUID = 1L;
}
