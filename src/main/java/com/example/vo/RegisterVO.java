package com.example.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterVO {
    /**
     * 访问令牌（Access Token）
     * 前端每次请求时，放在 Header 的 Authorization 字段中
     * 格式：Bearer {accessToken}
     */
    private String accessToken;

    /**
     * 令牌类型（固定为 Bearer）
     * 告诉前端拼接 Header 时的格式
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 过期时间（单位：秒）
     * 比如 604800 表示 7 天后过期
     * 前端可以根据这个时间设置定时器，在过期前主动刷新
     */
    private Long expiresIn;


    /**
     * 刷新令牌（Refresh Token）
     * 当 accessToken 过期后，前端可以用这个去换一个新的 accessToken
     * 避免用户重新登录
     *
     * ⚠️ 注意：如果你的项目暂时没有实现刷新接口，可以先不返回这个字段
     * 但商业项目通常都有，所以我先加上了
     */
    private String refreshToken;

    /**
     * 当前登录用户的基本信息
     * 包含：id、username、nickname、avatar、email 等
     * 前端直接拿来显示，不用再调接口查询
     *
     * ⚠️ 注意：这里用 UserVO，因为它已经过滤掉了 password 和 salt
     */
    private UserVO userInfo;
}
