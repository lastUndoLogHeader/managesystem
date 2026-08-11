package com.example.vo;

import com.example.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户返回前端的视图对象（VO）
 *
 * 作用：专门用来把用户数据返回给前端显示
 *
 * 为什么要单独建一个 VO 而不直接用 User 实体？
 * 1. 安全：密码（password）和盐（salt）绝对不能返回给前端
 * 2. 格式：把日期（如 2026-08-07T10:30:00）格式化成人能看懂的字符串
 * 3. 灵活性：可以添加额外字段（比如把 1 转成 "男" 再返回）
 */
@Data
public class UserVO {

    // ============================================================
    // 1. 主键
    // ============================================================

    /**
     * 用户ID
     *
     * 来源：User.id
     * 用途：前端可以用这个 ID 作为标识
     * 示例：1
     */
    private Long id;

    // ============================================================
    // 2. 登录相关（注意：没有 password 和 salt！）
    // ============================================================

    /**
     * 登录用户名
     *
     * 来源：User.username
     * 用途：显示用户登录名
     * 示例：zhangsan
     *
     * ⚠️ 注意：password 和 salt 字段被故意省略了！
     * 因为这两个是敏感信息，绝对不能暴露给前端。
     */
    private String username;

    // ⚠️ 这里没有 password 字段（安全原因，加密密文也不能暴露给前端）
    // ⚠️ 这里没有 salt 字段（安全原因，盐值也不能暴露给前端）

    // ============================================================
    // 3. 个人信息
    // ============================================================

    /**
     * 用户昵称（显示用）
     *
     * 来源：User.nickname
     * 用途：在页面上显示用户的名称
     * 示例：张三
     * 注意：如果为空，前端可以显示 username 作为备选
     */
    private String nickname;

    /**
     * 头像图片的 URL
     *
     * 来源：User.avatar
     * 用途：前端用这个 URL 显示用户头像
     * 示例：https://example.com/avatar/1.jpg
     */
    private String avatar;

    /**
     * 电子邮箱
     *
     * 来源：User.email
     * 用途：显示邮箱地址
     * 示例：zhangsan@test.com
     */
    private String email;

    /**
     * 手机号码
     *
     * 来源：User.phone
     * 用途：显示手机号
     * 示例：13800138000
     */
    private String phone;

    /**
     * 性别（数字代码）
     *
     * 来源：User.gender
     * 用途：前端根据这个数字显示对应的文字
     * 取值说明：
     *   0 = 未知
     *   1 = 男
     *   2 = 女
     * 示例：1
     *
     * 如果你想让前端直接拿到文字（"男"、"女"），
     * 可以在这个类里加一个 genderText 字段，在 fromEntity 里转换。
     */
    private Integer gender;

    /**
     * 出生日期
     *
     * 来源：User.birthday
     * 用途：显示用户的出生日期
     * 示例：1990-05-15
     *
     * @JsonFormat 注解作用：告诉 Jackson（Spring 默认的 JSON 转换工具）
     * 把这个 LocalDate 对象转成 "yyyy-MM-dd" 格式的字符串
     * 如果不加这个注解，前端会收到一个复杂的时间对象，很难处理
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate birthday;

    // ============================================================
    // 4. 权限与状态
    // ============================================================

    /**
     * 角色权限
     *
     * 来源：User.role
     * 取值说明：
     *   USER  = 普通用户
     *   ADMIN = 管理员
     * 示例：USER
     */
    private String role;

    /**
     * 账号状态
     *
     * 来源：User.status
     * 取值说明：
     *   0 = 禁用/冻结
     *   1 = 正常启用
     * 示例：1
     */
    private Integer status;

    /**
     * 逻辑删除标记
     *
     * 来源：User.isDeleted
     * 取值说明：
     *   0 = 未删除
     *   1 = 已删除
     * 示例：0
     *
     * 注意：正常查询时，已删除的数据不会被查出来，
     * 所以这个字段前端基本用不到，但保留可以方便调试。
     */
    private Integer isDeleted;

    // ============================================================
    // 5. 登录记录
    // ============================================================

    /**
     * 最后一次登录的 IP 地址
     *
     * 来源：User.lastLoginIp
     * 示例：192.168.1.100
     */
    private String lastLoginIp;

    /**
     * 最后一次登录的时间
     *
     * 来源：User.lastLoginTime
     * 示例：2026-08-07 14:30:00
     *
     * @JsonFormat 注解作用：把 LocalDateTime 转成 "yyyy-MM-dd HH:mm:ss" 格式
     * timezone = "GMT+8" 表示使用中国时区（东八区）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    // ============================================================
    // 6. 时间戳
    // ============================================================

    /**
     * 账号创建时间
     *
     * 来源：User.createTime
     * 示例：2026-01-01 10:00:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 账号信息最后更新时间
     *
     * 来源：User.updateTime
     * 示例：2026-08-07 14:30:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // ============================================================
    // 工具方法：把 User 转换成 UserVO
    // ============================================================

    /**
     * 将 User 实体转换为 UserVO
     *
     * 作用：在 Controller 或 Service 中，调用这个方法就能得到一个
     * 安全的、格式化好的 VO 对象。
     *
     * 用法：UserVO vo = UserVO.fromEntity(user);
     *
     * 为什么写这个方法？
     * 因为每次返回数据时，都需要把 User 对象的字段一个一个
     * 复制到 UserVO 里，写多了很麻烦，封装成方法方便复用。
     *
     * 特别注意：这里复制了除了 password 和 salt 之外的所有字段！
     * 你仔细看，没有 setPassword() 也没有 setSalt()，
     * 这样就保证了敏感信息永远不会被返回给前端。
     */
    public static UserVO fromEntity(User user) {
        // 第一步：如果传入的 user 是 null，直接返回 null
        // 防止后面的代码出现空指针异常
        if (user == null) {
            return null;
        }

        // 第二步：创建一个空的 VO 对象
        UserVO vo = new UserVO();

        // 第三步：一个一个字段复制（手动搬运）
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        // ⚠️ 注意：这里没有 setPassword()，没有 setSalt()！
        // 密码和盐值被故意省略了，永远不会返回给前端
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setIsDeleted(user.getIsDeleted());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());

        // 第四步：返回填好的 VO 对象
        return vo;
    }

    // ============================================================
    // 如果你想扩展：返回性别文字
    // ============================================================
    // 如果你不想让前端处理 0/1/2，而是直接返回 "男"/"女"/"未知"
    // 可以取消下面这段代码的注释：

    // /**
    //  * 性别文字（给前端直接显示用）
    //  *
    //  * 这个字段不是从数据库来的，而是根据 gender 转换出来的
    //  * 这样前端就不需要自己写 if/else 来转换了
    //  */
    // private String genderText;
    //
    // // 在 fromEntity 方法的最后加上这段：
    // // if (user.getGender() != null) {
    // //     switch (user.getGender()) {
    // //         case 1: vo.setGenderText("男"); break;
    // //         case 2: vo.setGenderText("女"); break;
    // //         default: vo.setGenderText("未知"); break;
    // //     }
    // // }
}