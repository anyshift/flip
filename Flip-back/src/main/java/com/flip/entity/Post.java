package com.flip.entity;

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

    private Double priority;

    private Integer type;  /* 0-正常; 1-置顶; 2-全局置顶 */

    private Integer status; /* 0-正常; 1-精华; 2-拉黑 */

    @TableField(fill = FieldFill.INSERT) /* 帖子发布时间，在插入记录时自动填充 */
    private String createTime;

    private Integer replyNumber; /*帖子被回复数*/

    private Integer viewNumber; /*帖子被查看数*/

    @TableField(fill = FieldFill.INSERT) /* 帖子发布时间，在插入记录时自动填充 */
    private String lastCommentTime; /*当前帖子最新一条评论的时间*/

    //@NotEmpty(message = "标签不能为空")
    @TableField(exist = false)
    private List<Tag> tags; /*帖子标签*/

    @TableField(exist = false)
    private String author; /*主题作者*/

    @TableField(exist = false)
    private String nickname; /*主题作者的昵称*/

    @TableField(exist = false)
    private String avatar; /*主题作者的头像*/
}
