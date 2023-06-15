package com.flip.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.flip.validation.VG;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TagOption {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "类型的显示名称不能为空", groups = VG.First.class)
    @Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z0-9]+$", message = "类型的显示名称不能包含特殊字符", groups = VG.Second.class)
    private String name;

    @NotBlank(message = "类型的英文标识不能为空", groups = VG.Third.class)
    @Pattern(regexp = "^[A-Za-z\\d\\-]+$", message = "类型英文标识不能包含特殊字符", groups = VG.Fourth.class)
    private String label;
}
