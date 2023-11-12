package com.flip.domain.entity;

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

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;

    /**
     * 用户UID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long uid;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = VG.First.class)
    @Length(min = 4, max = 12, message = "最少4个字符，最多12个字符", groups = VG.Second.class)
    @Pattern(regexp = "^[A-Za-z\\d]+$", message = "仅限英文字符或数字", groups = VG.Third.class)
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户邮箱
     */
    @NotBlank(message = "邮箱不能为空", groups = VG.Fourth.class)
    @Email(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "不支持的邮箱格式", groups = VG.Fifth.class)
    private String email;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空", groups = VG.Sixth.class)
    @Length(min = 8, message = "密码长度最少八位", groups = VG.Seventh.class)
    @Pattern(regexp = "^(?=.*\\d.*)(?=.*[A-Z].*)(?=.*[a-z].*)(?=.*[`~!@#$%^&*()_\\-+=<>.?:\"{}].*).{8,20}$", message = "需同时包含大小写字母、数字和特殊符号", groups = VG.Eighth.class)
    private String password;

    /**
     * 邮箱认证状态
     */
    private Boolean emailVerified;

    /**
     * 用户盐值
     */
    private String salt;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 资料被谁更新
     */
    private String updatedBy;

    /**
     * 注册时间
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    /**
     * 更新账号信息时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

    /**
     * 是否被禁用。{ false:未禁用, true:已禁用 }
     */
    private Boolean banned;

    /**
     * 是否被（逻辑）删除。{ false:未删除, true:已删除 }
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 用户具有的权限
     */
    @TableField(exist = false)
    private Authority authority;

    /**
     * 用户的角色信息
     */
    @TableField(exist = false)
    private Role role;

}
