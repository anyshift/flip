package com.flip.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long pid;
    private Long fromUid;
    private Long toUid;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Integer parentId; /*子级回复的父级回复ID，根级评论为0*/
    private Integer replyId; /*楼中楼中回复目标楼层的ID*/

    @TableField(fill = FieldFill.INSERT) /* 评论or回复时间，在插入记录时自动填充 */
    private String createTime;

    @TableField(exist = false)
    private String fromUsername; /*评论或回复者的用户名*/

    @TableField(exist = false)
    private String fromNickname; /*评论或回复者的昵称*/

    @TableField(exist = false)
    private String fromAvatar; /*评论或回复者的头像*/

    @TableField(exist = false)
    private String toUsername; /*被评论或被回复者的用户名*/

    @TableField(exist = false)
    private String toNickname; /*被评论或被回复者的昵称*/

    @TableField(exist = false)
    private Integer repliesNum; /*评论对应的回复数量*/
}
