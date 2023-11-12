package com.flip.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.entity.Comment;
import com.flip.mapper.CommentMapper;
import com.flip.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Override
    public List<Comment> getComments(String pid) {
        return commentMapper.getComments(pid);
    }

    @Override
    public Integer getRepliesNumOfComment(Integer commentId) {
        return commentMapper.getRepliesNumOfComment(commentId);
    }

    @Override
    public List<Comment> getReplies(String pid, Integer parentId) {
        return commentMapper.getReplies(pid, parentId);
    }

}
