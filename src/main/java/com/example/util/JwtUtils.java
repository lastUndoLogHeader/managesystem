package com.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// 这个类的作用：专门用来生成Token和解析Token
@Slf4j
@Component
public class JwtUtils {

    // ------------------- 第1个知识点：密钥（SecretKey） -------------------
    // 解释：这是“印章”的模具。生成签名和验证签名必须用同一个密钥。
    // 注意：我这里为了演示写死了，实际中绝对不能写死在代码里，要放在配置文件中！
    // 这里密钥长度必须 >= 256位（即32个字符），我这里写了64个字符，绝对安全。


    @Value("${jwt.secret}")
    private String secretString;

    @Value(("${jwt.access-expire-time}"))
    private Long accessExpireTime;

    @Value(("${jwt.refresh-expire-time}"))
    private Long refreshExpireTime;

    // 用这个字符串生成一个JWT专用的安全密钥对象（HS256算法）
    private SecretKey secretKey;


    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        log.info("JwtUtils 初始化成功，密钥长度：{} 字节", secretKey.getEncoded().length);
    }

    // ============================================================
    // 功能1：生成Token（制票）
    // 入参：用户的唯一ID（比如从数据库查出来的主键），用户的用户名
    // 返回：一串长长的、乱码一样的字符串
    // ============================================================
    private String generateToken(Long userId, String username, Long expireTime) {

        // 1. 获取当前时间
        Date now = new Date();
        // 2. 计算过期时间（当前时间 + 我们设定的7天）
        Date expireDate = new Date(now.getTime() + expireTime);

        // 3. 开始建造Token（链式调用，一行行看）
        String token = Jwts.builder()    // 创建一个JWT建造器
                // 【载荷部分】设置主题：通常放用户的唯一标识（这里放用户ID转成字符串）
                .subject(String.valueOf(userId))
                // 【载荷部分】自定义放一个键值对（key-value），比如存放用户名
                // 这就像票面上额外写了一句“姓名：张三”
                .claim("username", username)
                // 【载荷部分】签发时间：告诉别人这张票是什么时候发的
                .issuedAt(now)
                // 【载荷部分】过期时间：告诉别人这张票什么时候失效
                .expiration(expireDate)
                // 【头部+签名部分】用我们的密钥进行签名，并指定算法为HS256
                // 这一步就像在票上“盖印章”，防止别人乱改票面信息
                .signWith(secretKey)
                // 最后，把所有信息压缩成那个“乱码字符串”
                .compact();

        return token;
    }

    /**
     * 生成 AccessToken（短期票）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return AccessToken 字符串
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, accessExpireTime);
    }

    /**
     * 生成 RefreshToken（长期票）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return RefreshToken 字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, refreshExpireTime);
    }

    /**
     * 获取 Token 的过期时间（单位：秒）
     * 前端需要知道这个时间来设置定时器
     */
    public long getAccessExpireTime() {
        return this.accessExpireTime / 1000;
    }

    /**
     * 获取 RefreshToken 过期时间（单位：秒）
     */
    public long getRefreshExpireTime() {
        return this.refreshExpireTime / 1000;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = this.parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = this.parseToken(token);
        return claims.get("username", String.class);
    }

    // ============================================================
    // 功能2：解析Token（验票）
    // 入参：前端传过来的那个“乱码字符串”
    // 返回：解析后得到的“载荷”对象（里面放着所有信息）
    // 注意：如果Token过期、或者被人篡改过，这行代码会直接报错！
    // ============================================================
    public Claims parseToken(String token) {

        // 1. 创建一个解析器，传入我们的密钥（必须和生成时一模一样）
        // 2. 把token字符串丢进去解析
        // 3. 验证签名（如果签名不对，这一步就抛出异常）
        // 4. 获取解析出来的“载荷”部分（即Payload）
        Claims claims = Jwts.parser()          // 创建解析器
                .verifyWith(secretKey)         // 传入验章的“模具”（密钥）
                .build()                        // 建造解析器
                .parseSignedClaims(token)       // 把Token丢进去解析并验证签名
                .getPayload();                  // 验证通过后，取回票面信息（载荷）

        return claims;
    }
}