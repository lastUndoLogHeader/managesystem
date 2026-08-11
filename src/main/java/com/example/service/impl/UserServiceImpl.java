package com.example.service.impl;

import com.example.common.ResultCode;
import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.UserMapper;
import com.example.service.MailService;
import com.example.service.UserService;
import com.example.util.BloomFilterUtil;
import com.example.util.JwtUtils;
import com.example.util.RedisUtil;
import com.example.vo.LoginVO;
import com.example.vo.RegisterVO;
import com.example.vo.UserVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private BloomFilterUtil bloomFilterUtil;
    @Autowired
    private MailService mailService;

    @Override
    public UserVO findUserById(Long id) {
        User user = userMapper.selectUserById(id);
        if (user == null) {
            log.error("用户不存在，用户id：{}", id);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserVO userVO = UserVO.fromEntity(user);
        return userVO;
    }

    @Override
    public UserVO findUserByUsername(String username) {
        boolean mightContainUsername = bloomFilterUtil.mightContainUsername(username);
        if (!mightContainUsername) {
            log.warn("布隆过滤器确定该用户名不存在，用户名：{}", username);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        User user = userMapper.selectUserByUsername(username);
        if (user == null) {
            log.warn("用户不存在，用户名：{}", username);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return UserVO.fromEntity(user);
    }

    @Override
    @Transactional
    public RegisterVO register(User user) {

        User storedUser = userMapper.selectUserByUsername(user.getUsername());
        if (storedUser != null) {
            // 这是一个业务异常，不是系统异常，直接返回错误提示即可
            log.warn("注册失败，用户名已存在：{}", user.getUsername());
            throw new BusinessException(ResultCode.USER_EXIST);
        }
        if (user.getGender() == null) {
            user.setGender(0);
        }
        // 在 UserServiceImpl.register 方法里
        if (user.getBirthday() != null) {
            LocalDate today = LocalDate.now();
            int age = Period.between(user.getBirthday(), today).getYears();
            if (age < 0 || age > 120) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "出生日期不合法（年龄必须在 0-120 岁之间）");
            }
        }
        user.setUsername(user.getUsername().trim());
        user.setPassword(user.getPassword().trim());  // 注意：密码加密前再 trim
        String encode = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode);
        Integer rows = userMapper.insertUser(user);
        log.info("插入成功，主键id是:{}", user.getId());
        bloomFilterUtil.addUsername(user.getUsername());
        log.info("新用户名被添加到布隆过滤器，用户名：{}", user.getUsername());

        //注册成功发送邮件
        mailService.sendSimpleMail("2845624577@qq.com", "nihao", "nihao");

        // 在 register 方法里，插入成功后生成 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());
        long accessExpireTime = jwtUtils.getAccessExpireTime();
        long refreshExpireTime = jwtUtils.getRefreshExpireTime();
        redisUtil.saveRefreshToken(user.getId(), refreshToken, refreshExpireTime);

        // 组装注册响应
        UserVO userVO = UserVO.fromEntity(user);
        RegisterVO registerVO = RegisterVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpireTime)  // 7天
                .userInfo(userVO)
                .build();
        return registerVO;
    }

    @Override
    @Transactional
    public LoginVO login(LoginRequest loginRequest, HttpServletRequest request) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        boolean isContained = bloomFilterUtil.mightContainUsername(username);
        if (!isContained) {
            log.warn("布隆过滤器确定用户名不存在，用户名：{}", username);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        User user = userMapper.selectUserByUsername(username);
        if (user == null) {
            log.warn("用户名不存在，用户名：{}", username);
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_WRONG);
        }

        String encryptedPassword = user.getPassword();
        boolean matches = passwordEncoder.matches(password, encryptedPassword);
        if (!matches) {
            log.warn("登录失败，密码错误，用户名：{}", username);
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_WRONG);
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp(request));
        userMapper.updateUser(user);

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());
        long accessExpireTime = jwtUtils.getAccessExpireTime();
        long refreshExpireTime = jwtUtils.getRefreshExpireTime();

        redisUtil.saveRefreshToken(user.getId(), refreshToken, refreshExpireTime);

        UserVO userVO = UserVO.fromEntity(user);
        LoginVO loginVO = LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpireTime)
                .userInfo(userVO)
                .build();
        return loginVO;
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        Claims claims;
        try {
            claims = jwtUtils.parseToken(refreshToken);
        } catch (Exception e) {
            log.warn("RefreshToken 无效或已过期：{}", e.getMessage());
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);

        String storedRefreshToken = redisUtil.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            log.warn("RefreshToken 不匹配或已被撤销，用户 ID：{}", userId);
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        User user = userMapper.selectUserById(userId);
        if (user == null) {
            log.error("用户不存在，用户 ID：{}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String newAccessToken = jwtUtils.generateAccessToken(userId, username);
        LoginVO loginVO = LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getAccessExpireTime())
                .userInfo(UserVO.fromEntity(user))
                .build();
        return loginVO;
    }

    @Override
    public void logout(String token) {
        Claims claims;
        Long userId = null;
        try {
            claims = jwtUtils.parseToken(token);
            userId = Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            // ⭐ 场景2：Token 过期了
            // 用户很久没操作，Token 已经失效。
            // 此时 Redis 里的 RefreshToken 大概率也被淘汰了，不需要额外操作。
            // 我们直接放行，让前端清除本地缓存即可。
            log.info("用户退出登录时 Token 已过期，无需后端额外清理，直接放行。");
            return;  // ← 直接返回，不抛异常！
        } catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
            // ⭐ 场景3：Token 无效或被篡改
            // 这种情况也直接放行，因为无法从 Token 中获取有效信息，
            // 也无法执行任何有意义的清理操作。
            log.warn("用户退出登录时 Token 无效（格式错误或被篡改），直接放行。错误：{}", e.getMessage());
            return;  // ← 直接返回，不抛异常！
        } catch (Exception e) {
            // 其他未知异常（理论上不会发生）
            log.warn("退出登录时发生未知异常，直接放行。错误：{}", e.getMessage());
            return;  // ← 直接返回，不抛异常！
        }
        if (userId != null) {
            redisUtil.deleteRefreshToken(userId);

            Long remainingSeconds = getRemainingSeconds(claims);
            if (remainingSeconds > 0) {
                redisUtil.addToBlackList(userId, token, remainingSeconds);
                log.info("AccessToken 已加入黑名单，用户 ID：{}，黑名单有效期：{} 秒", userId, remainingSeconds);
            } else {
                log.info("Token 已过期，无需加入黑名单，用户 ID：{}", userId);
            }

            log.info("用户退出登录成功，已清除 Redis 中的 RefreshToken，用户 ID：{}", userId);
        }
    }

    public Long getRemainingSeconds(Claims claims) {
        try {
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remainingMillis = expiration.getTime() - now.getTime();
            if (remainingMillis <= 0) {
                return 0l;
            }
            return remainingMillis / 1000;
        } catch (Exception e) {
            // Token 解析失败（过期、无效等），返回 0
            log.warn("计算 Token 剩余有效期失败，默认返回 0：{}", e.getMessage());
            return 0l;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果经过多个代理，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}


