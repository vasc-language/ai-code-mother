package com.spring.aicodemother.model.dto.app;

import com.spring.aicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询应用版本请求
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppVersionQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 版本号
     */
    private Integer versionNum;

    /**
     * 版本标签
     */
    private String versionTag;

    /**
     * 部署操作用户id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
