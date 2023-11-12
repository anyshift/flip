package com.flip.domain.entity;

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

    /**
     * 评论或回复对应的帖子ID
     */
    private Long pid;

    /**
     * 回复者UID
     */
    private Long fromUid;

    /**
     * 被回复者UID
     */
    private Long toUid;

    /**
     * 评论或回复的内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 子级回复的父级回复ID，根级评论值为0
     */
    private Integer parentId;

    /**
     * 楼中楼中回复目标楼层的ID
     */
    private Integer replyId;

    /**
     * 评论or回复时间，在插入记录时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    /**
     * 论或回复者的用户名
     */
    @TableField(exist = false)
    private String fromUsername;

    /**
     * 评论或回复者的昵称
     */
    @TableField(exist = false)
    private String fromNickname;

    /**
     * 评论或回复者的头像
     */
    @TableField(exist = false)
    private String fromAvatar;

    /**
     * 被评论或被回复者的用户名
     */
    @TableField(exist = false)
    private String toUsername;

    /**
     * 被评论或被回复者的昵称
     */
    @TableField(exist = false)
    private String toNickname;

    /**
     * 评论对应的回复数量
     */
    @TableField(exist = false)
    private Integer repliesNum;

}
