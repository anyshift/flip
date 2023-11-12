package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<Comment> getComments(String pid);

    Integer getRepliesNumOfComment(Integer commentId);

    List<Comment> getReplies(String pid, Integer parentId);

}
