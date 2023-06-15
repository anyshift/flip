package com.flip.entity;

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

    @NotNull(message = "标签类型不能为空", groups = VG.Fifth.class)
    private Integer optionId;

    @NotBlank(message = "标签名不能为空", groups = VG.First.class)
    @Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z0-9]+$", message = "标签名不能包含特殊字符", groups = VG.Second.class)
    private String name;

    @NotBlank(message = "标签英文标识不能为空", groups = VG.Third.class)
    @Pattern(regexp = "^[A-Za-z\\d\\-]+$", message = "标签英文标识不能包含特殊字符", groups = VG.Fourth.class)
    private String label;

    private String icon;

    private String detail;

    private Long creator;

    @TableField(fill = FieldFill.INSERT) /* 在插入记录时自动填充 */
    private String createTime;

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
