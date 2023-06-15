package com.flip.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostTag {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long pid;

    private String tagLabel;

    private Long creator;

    @TableField(fill = FieldFill.INSERT)
    private String createTime;
}
