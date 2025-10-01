package com.spring.aicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 版本回滚请求
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
public class AppVersionRollbackRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 要回滚到的版本id
     */
    private Long versionId;

    private static final long serialVersionUID = 1L;
}
