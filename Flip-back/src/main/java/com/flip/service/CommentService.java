package com.flip.service;

import com.flip.domain.Response;
import com.flip.entity.Comment;

public interface CommentService {
    Response<Object> getComments(String pid);

    Response<Object> getReplies(String pid, Integer parentId);

    Response<Object> doComment(Comment comment);

    Response<Object> doReply(Comment comment);
}
