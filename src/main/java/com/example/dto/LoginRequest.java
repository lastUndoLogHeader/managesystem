package com.example.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class LoginRequest {
    // ============================================================
    // 2. 登录相关（必须校验！）
    // ============================================================

    /**
     * 登录用户名
     *
     * 校验规则：
     * - 不能为空（@NotBlank）
     * - 长度 4-20 个字符（@Size）
     * - 只能包含字母、数字、下划线（@Pattern）
     *
     * 为什么这样限制？
     * - 太短（如 "a"）容易被暴力破解
     * - 太长（超过 20）浪费数据库空间
     * - 特殊字符（如 @#￥%）可能导致 SQL 注入或显示问题
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在 4-20 个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 登录密码
     *
     * 校验规则：
     * - 不能为空（@NotBlank）
     * - 长度 8-32 个字符（@Size）
     * - 必须包含至少一个字母和一个数字（@Pattern）
     *
     * 为什么这样限制？
     * - 太短（如 "123456"）容易被破解
     * - 太长（超过 32）用户记不住，且加密后密文长度固定，输入长度不影响存储
     * - 要求字母+数字组合，避免纯数字或纯字母的弱密码
     *
     * ⚠️ 注意：密码的"字母+数字"只是最基础要求，
     * 商业项目通常还会要求包含大小写字母和特殊字符，
     * 但这里先做最基本限制，以免注册门槛太高。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在 8-32 个字符之间")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "密码必须同时包含字母和数字"
    )
    private String password;
}
