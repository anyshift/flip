package com.flip.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.flip.validation.VG;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 标签类型
     */
    @NotNull(message = "标签类型不能为空", groups = VG.Fifth.class)
    private Integer optionId;

    /**
     * 标签名
     */
    @NotBlank(message = "标签名不能为空", groups = VG.First.class)
    @Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z0-9]+$", message = "标签名不能包含特殊字符", groups = VG.Second.class)
    private String name;

    /**
     * 标签英文标识
     */
    @NotBlank(message = "标签英文标识不能为空", groups = VG.Third.class)
    @Pattern(regexp = "^[A-Za-z\\d\\-]+$", message = "标签英文标识不能包含特殊字符", groups = VG.Fourth.class)
    private String label;

    /**
     * 标签图标
     */
    private String icon;

    /**
     * 标签详情
     */
    private String detail;

    /**
     * 标签创建者
     */
    private Long creator;

    /**
     * 标签创建时间，在插入记录时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    /**
     * 标签选项
     */
    @TableField(exist = false)
    private TagOption tagOption;

    public Tag(Integer optionId, String name, String label, String icon, String detail) {
        this.optionId = optionId;
        this.name = name;
        this.label = label;
        this.icon = icon;
        this.detail = detail;
    }

}
