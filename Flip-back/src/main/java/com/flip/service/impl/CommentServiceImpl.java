package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.Response;
import com.flip.entity.Comment;
import com.flip.entity.Post;
import com.flip.mapper.CommentMapper;
import com.flip.mapper.PostMapper;
import com.flip.service.CommentService;
import com.flip.service.SensitiveWordService;
import com.flip.utils.SensitiveWordUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private PostMapper postMapper;

    @Override
    public Response<Object> getComments(String pid) {
        List<Comment> comments = commentMapper.getComments(pid);

        if (ObjectUtil.isNotNull(comments)) {
            for (Comment comment: comments) {
                comment.setContent(HtmlUtil.unescape(comment.getContent()));
                comment.setRepliesNum(commentMapper.getRepliesNumOfComment(comment.getId()));
            }
            return Response.success(200, Map.of("comments", comments));
        }

        return Response.success(400, "获取评论列表失败");
    }

    @Override
    public Response<Object> getReplies(String pid, Integer parentId) {
        List<Comment> replies = commentMapper.getReplies(pid, parentId);
        if (ObjectUtil.isNotNull(replies)) {
            for (Comment reply: replies) {
                reply.setContent(HtmlUtil.unescape(reply.getContent()));
            }
            return Response.success(200, Map.of("replies", replies));
        }
        return Response.error(400, "获取回复列表失败");
    }

    @Override
    @Transactional
    public Response<Object> doComment(Comment comment) {
        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String commentContent = SensitiveWordUtils.stringSearchEx2Filter(comment.getContent(), sensitiveWords);
        comment.setContent(HtmlUtil.escape(commentContent));
        comment.setParentId(0);
        comment.setReplyId(0);
        int insertResult = commentMapper.insert(comment);

        if (insertResult > 0) {
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", comment.getPid())
                    .setSql("reply_number = reply_number + 1")
                    .setSql("last_comment_time = '" + comment.getCreateTime() + "'");
            int updateResult = postMapper.update(null, updateWrapper);

            if (updateResult > 0) {
                comment.setContent(commentContent);
                return Response.success(200, "评论成功", Map.of("comment", comment));
            }
        }

        return Response.success(400, "评论失败");
    }

    @Override
    public Response<Object> doReply(Comment comment) {
        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String replyText = SensitiveWordUtils.stringSearchEx2Filter(comment.getContent(), sensitiveWords);
        comment.setContent(HtmlUtil.escape(replyText));

        int insertResult = commentMapper.insert(comment);
        if (insertResult > 0) {
            comment.setContent(replyText);
            return Response.success(200, "回复成功", Map.of("reply", comment));
        }

        return Response.error(400, "回复失败");
    }
}
