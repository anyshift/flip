package com.flip.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.flip.common.Response;
import com.flip.domain.entity.Comment;
import com.flip.domain.entity.Post;
import com.flip.service.CommentService;
import com.flip.service.PostService;
import com.flip.service.SensitiveWordService;
import com.flip.utils.SensitiveWordUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private PostService postService;

    @GetMapping("/getComments")
    public Response<Map<String, Object>> getComments(@RequestParam String pid) {
        List<Comment> comments = commentService.getComments(pid);
        if (ObjectUtil.isNotNull(comments)) {
            for (Comment comment: comments) {
                comment.setContent(HtmlUtil.unescape(comment.getContent()));
                comment.setRepliesNum(commentService.getRepliesNumOfComment(comment.getId()));
            }
            return Response.success("获取评论列表成功", Map.of("comments", comments));
        }
        return Response.failed("获取评论列表失败");
    }

    @GetMapping("/getReplies")
    public Response<Object> getReplies(@RequestParam String pid, @RequestParam Integer parentId) {
        List<Comment> replies = commentService.getReplies(pid, parentId);
        if (ObjectUtil.isNotNull(replies)) {
            for (Comment reply: replies) {
                reply.setContent(HtmlUtil.unescape(reply.getContent()));
            }
            return Response.success("获取回复列表成功", Map.of("replies", replies));
        }
        return Response.failed("获取回复列表失败");
    }

    @PostMapping("/doComment")
    public Response<Map<String, Comment>> doComment(@RequestBody Comment comment) {
        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String commentContent = SensitiveWordUtils.stringSearchEx2Filter(comment.getContent(), sensitiveWords);
        comment.setContent(HtmlUtil.escape(commentContent));
        comment.setParentId(0);
        comment.setReplyId(0);

        boolean saved = commentService.save(comment);
        if (saved) {
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getPid())
                    .setSql("reply_number = reply_number + 1")
                    .setSql("last_comment_time = '" + comment.getCreateTime() + "'");

            boolean updated = postService.update(null, updateWrapper);
            if (updated) {
                comment.setContent(commentContent);
                return Response.success("评论成功", Map.of("comment", comment));
            }
        }
        return Response.failed("评论失败");
    }

    @PostMapping("/doReply")
    public Response<Map<String, Comment>> doReply(@RequestBody Comment comment) {
        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String replyText = SensitiveWordUtils.stringSearchEx2Filter(comment.getContent(), sensitiveWords);
        comment.setContent(HtmlUtil.escape(replyText));

        boolean saved = commentService.save(comment);
        if (saved) {
            comment.setContent(replyText);
            return Response.success("回复成功", Map.of("reply", comment));
        }

        return Response.failed("回复失败");
    }
}
