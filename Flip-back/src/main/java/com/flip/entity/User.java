package com.flip.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.flip.validation.VG;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

/**
 * 用户实体类。
 * 标注在属性上的各种注解来自 validation 依赖，用于校验属性是否满足给定要求，如果不满足会抛出 MethodArgumentNotValidException 异常。
 * 在需要校验的地方使用 @Validated 或 @Valid，如：public void test(@Valid User user)。
 * 注解 @Validated 和注解 @Valid 的作用是一致的，不同之处在于两个注解的使用范围不同。
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;

    @TableId(type = IdType.ASSIGN_ID)
    private Long uid;

    private String avatar;

    @NotBlank(message = "用户名不能为空", groups = VG.First.class)
    @Length(min = 4, max = 12, message = "最少4个字符，最多12个字符", groups = VG.Second.class)
    @Pattern(regexp = "^[A-Za-z\\d]+$", message = "仅限英文字符或数字", groups = VG.Third.class)
    private String username;

    private String nickname;

    @NotBlank(message = "邮箱不能为空", groups = VG.Fourth.class)
    @Email(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "不支持的邮箱格式", groups = VG.Fifth.class)
    private String email;

    @NotBlank(message = "密码不能为空", groups = VG.Sixth.class)
    @Length(min = 8, message = "密码长度最少八位", groups = VG.Seventh.class)
    @Pattern(regexp = "^(?=.*\\d.*)(?=.*[A-Z].*)(?=.*[a-z].*)(?=.*[`~!@#$%^&*()_\\-+=<>.?:\"{}].*).{8,20}$", message = "需同时包含大小写字母、数字和特殊符号", groups = VG.Eighth.class)
    private String password;

    private Boolean emailVerified;

    private String salt; /* 盐值 */

    private String registerIp; /* 注册IP */

    private String updatedBy; /* 被谁更新账户资料 */

    @TableField(fill = FieldFill.INSERT) /* 在插入记录时自动填充 */
    private String createTime; /* 注册时间 */

    @TableField(fill = FieldFill.UPDATE) /* 在更新记录时自动填充 */
    private String updateTime; /* 更新账户时间 */

    private Boolean banned; /* { false:未禁用, true:已禁用 } */

    @TableLogic
    private Boolean deleted; /* { false:未删除, true:已删除 } */

    @TableField(exist = false)
    private Authority authority; /* 当前用户的所有权限 */

    @TableField(exist = false)
    private Role role; /* 当前用户所有角色信息 */
}
