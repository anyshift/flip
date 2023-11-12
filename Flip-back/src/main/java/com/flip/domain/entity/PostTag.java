package com.flip.domain.entity;

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

    /**
     * 帖子ID
     */
    private Long pid;

    /**
     * 帖子标签
     */
    private String tagLabel;

    /**
     * 标签创建者
     */
    private Long creator;

    /**
     * 标签创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

}
