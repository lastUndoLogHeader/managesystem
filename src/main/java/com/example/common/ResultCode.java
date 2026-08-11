package com.example.common;

public enum ResultCode {
    // 1. 通用成功与失败（成功固定为 0，不要用 200）
    SUCCESS(0, "操作成功"),
    ERROR(9999, "系统繁忙，请稍后重试"),

    // 2. 参数/数据异常（1xxx）
    PARAM_ERROR(1001, "参数校验失败"),
    DATA_NOT_EXIST(1002, "数据不存在"),
    DATA_EXIST(1003, "数据已存在"),

    // 3. 用户相关异常（3xxx，结合你的 JWT）
    USER_NOT_FOUND(3001, "用户不存在"),          // 对应你之前的查找用户不存在
    USER_EXIST(3002, "用户已存在"),          // 对应你之前的查找用户不存在
    USERNAME_OR_PASSWORD_WRONG(3003, "用户名或密码错误"),
    UNAUTHORIZED(3004, "未登录，请先登录"),     // 专门给 AuthenticationEntryPoint 用
    TOKEN_EXPIRED(3005, "登录已过期，请重新登录"),
    TOKEN_INVALID(3006, "无效的Token"),
    PERMISSION_DENIED(3007, "无权限访问该资源"),

    // 4. 业务逻辑异常（4xxx）
    BALANCE_NOT_ENOUGH(4001, "余额不足"),
    STOCK_NOT_ENOUGH(4002, "库存不足");

    private Integer code;
    private String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() { return code; }
    public String getMsg() { return msg; }
}