package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * ⚠️ 所有字段都加上了校验注解，前端传参时会自动验证
 * 如果校验失败，全局异常处理器会捕获并返回友好提示
 */
@Data
public class User {

    // ============================================================
    // 1. 主键（不需要校验，因为是数据库自动生成的）
    // ============================================================
    private Long id;

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

    /**
     * 密码盐值（可空，不需要校验）
     *
     * 注意：用 BCrypt 时这个字段用不到，但保留
     */
    private String salt;

    // ============================================================
    // 3. 个人信息（可空字段用 @Size 限制长度）
    // ============================================================

    /**
     * 用户昵称（可空）
     *
     * 校验规则：
     * - 长度 0-30 个字符（@Size）
     * - 不能包含特殊字符（@Pattern）
     *
     * 为什么限制 30？
     * 昵称太长了（如 100 个字符）在页面上显示会换行，影响 UI
     */
    @Size(max = 30, message = "昵称不能超过 30 个字符")
    @Pattern(
            regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_ ]*$",  // ← 把 \s 改成空格
            message = "昵称只能包含中文、字母、数字、下划线和空格"
    )
    private String nickname;

    /**
     * 头像 URL（可空）
     *
     * 校验规则：
     * - 长度 0-255（数据库字段限制）
     * - 必须是合法的 URL 格式（@URL）
     *
     * 为什么用 @URL？
     * 防止用户乱填一个字符串导致前端图片加载失败
     */
    @Size(max = 255, message = "头像地址不能超过 255 个字符")
    @URL(message = "头像地址必须是合法的 URL 格式（如 https://xxx.com/avatar.jpg）")
    private String avatar;

    /**
     * 电子邮箱（可空）
     *
     * 校验规则：
     * - 长度 0-128（数据库限制）
     * - 必须是合法的邮箱格式（@Email）
     */
    @Size(max = 128, message = "邮箱不能超过 128 个字符")
    @Email(message = "邮箱格式不正确（如 example@domain.com）")
    private String email;

    /**
     * 手机号码（可空）
     *
     * 校验规则：
     * - 长度 0-20（数据库限制）
     * - 必须符合中国手机号格式（@Pattern）
     *
     * 为什么用这个正则？
     * 11 位数字，以 1 开头，第二位是 3-9
     * 这是中国手机号的基本格式
     */
    @Size(max = 20, message = "手机号不能超过 20 个字符")
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确（必须是 11 位数字，以 1 开头）"
    )
    private String phone;

    /**
     * 性别
     *
     * 校验规则：
     * - 必须是 0、1、2 中的一个（@Min + @Max）
     *
     * 为什么限制？
     * 防止前端传 3、4、5 等无效值
     */
    @Min(value = 0, message = "性别只能为 0（未知）、1（男）、2（女）")
    @Max(value = 2, message = "性别只能为 0（未知）、1（男）、2（女）")
    private Integer gender;

    /**
     * 出生日期（可空）
     *
     * 校验规则：
     * - 不能是未来日期（@Past）
     *
     * 为什么用 @Past？
     * 用户不能选择未来的生日（比如 2099-01-01）
     */
    @PastOrPresent(message = "出生日期不能是未来的时间")
    private LocalDate birthday;

    // ============================================================
    // 4. 权限与状态（这些字段由后端控制，不需要前端传，所以不加校验）
    // ============================================================

    private String role;      // 默认 USER，后端设置
    private Integer status;   // 默认 1，后端设置
    private Integer isDeleted; // 默认 0，后端设置

    // ============================================================
    // 5. 登录记录（由后端填充，不需要前端传，所以不加校验）
    // ============================================================

    private String lastLoginIp;
    private LocalDateTime lastLoginTime;

    // ============================================================
    // 6. 时间戳（由数据库自动生成，不需要前端传，所以不加校验）
    // ============================================================

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}