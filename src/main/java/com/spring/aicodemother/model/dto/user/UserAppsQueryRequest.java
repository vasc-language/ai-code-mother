package com.spring.aicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户应用列表查询请求
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
public class UserAppsQueryRequest implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 页码
     */
    private long pageNum = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段（createTime/updateTime/appName）
     */
    private String sortBy = "createTime";

    /**
     * 排序方式（ascend/descend）
     */
    private String sortOrder = "descend";

    /**
     * 生成类型筛选（all/HTML/多文件项目/Vue应用）
     */
    private String genType = "all";

    private static final long serialVersionUID = 1L;
}