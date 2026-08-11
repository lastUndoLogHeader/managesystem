package com.example.common;

import lombok.Data;

/**
 * 统一返回结果的包装类（泛型版）
 * T 代表你要返回的具体数据是什么类型（比如 User，或者 List<User>）
 */
@Data
public class Result<T> {

    // 1. 状态码（比如 200 成功，500 失败）
    private Integer code;

    // 2. 提示消息（比如 "查询成功" 或 "用户名不存在"）
    private String msg;

    // 3. 真正的业务数据（比如查出来的 User 对象，T 就是 User）
    private T data;

    // ---------- 下面是构造方法（用于自己 new 对象） ----------
    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ---------- 下面是成功和失败的快捷方法（静态方法，重点！） ----------

    /**
     * 成功返回（不带数据），比如删除成功、修改成功
     */
    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功返回（带数据），比如查询用户详情、查询列表
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 失败返回（自定义提示消息），比如 "账号密码错误"
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }

    /**
     * 失败返回（自定义状态码和提示消息），比较灵活
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

}