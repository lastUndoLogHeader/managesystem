package com.example.common;

import com.example.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 * <p>
 * 作用：拦截所有 Controller 抛出的异常，统一转换成 Result 格式返回给前端
 * 这样 Controller 层就不需要写 try-catch 了
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 处理业务异常：业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        // 这里的 e.getMessage() 就是我们在 Service 里抛异常时写的提示语
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 在文件里加上这个方法（放在其他 @ExceptionHandler 方法的旁边）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 获取第一个校验失败的错误信息
        String errorMsg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败：{}", errorMsg);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 处理 GET 请求参数校验失败异常（@RequestParam 上的校验注解触发）
     *
     * 前端会收到：{"code": 1001, "msg": "用户 ID 必须大于 0", "data": null}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        // 从异常中取出第一条校验失败的错误信息
        String errorMsg = e.getConstraintViolations().iterator().next().getMessage();
        log.warn("参数校验失败：{}", errorMsg);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 处理运行时异常（兜底，所有没被上面捕获的异常都会来这里）
     * 前端会收到：{"code": 500, "msg": "系统繁忙，请稍后重试", "data": null}
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("系统异常：", e);
        return Result.error(ResultCode.ERROR.getCode(), "系统繁忙，请稍后重试");
    }

    /**
     * 处理所有其他异常（终极兜底）
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未知异常：", e);
        return Result.error(ResultCode.ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}