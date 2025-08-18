package com.spring.aicodemother.common;

import com.spring.aicodemother.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 * 
 * @param <T> 数据类型
 * @author system
 */
@Data
public class BaseResponse<T> implements Serializable {
    // code data message
    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
