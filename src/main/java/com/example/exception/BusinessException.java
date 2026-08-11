package com.example.exception;

import com.example.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    // 也可以支持自定义消息（覆盖枚举的默认msg）
    public BusinessException(ResultCode resultCode, String customMsg) {
        super(customMsg);
        this.code = resultCode.getCode();
    }

}
