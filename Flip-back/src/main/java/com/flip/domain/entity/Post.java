package com.flip.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @TableId
    private Long id;

    private Long uid;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 帖子优先级
     */
    private Double priority;

    /**
     * 帖子类型。0-正常; 1-置顶; 2-全局置顶
     */
    private Integer type;

    /**
     * 帖子状态类型。0-正常; 1-精华; 2-拉黑
     */
    private Integer status;

    /**
     * 帖子发布时间，在插入记录时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    /**
     * 帖子被回复数
     */
    private Integer replyNumber;

    /**
     * 帖子被查看数
     */
    private Integer viewNumber;

    /**
     * 当前帖子最新一条评论的时间，在插入记录时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private String lastCommentTime;

    /**
     * 帖子标签
     */
    @TableField(exist = false)
    private List<Tag> tags;

    /**
     * 作者用户名
     */
    @TableField(exist = false)
    private String author;

    /**
     * 作者昵称
     */
    @TableField(exist = false)
    private String nickname;

    /**
     * 作者头像
     */
    @TableField(exist = false)
    private String avatar;

}
