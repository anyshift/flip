package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    List<Comment> getComments(String pid);
    Integer getRepliesNumOfComment(Integer parentId);

    List<Comment> getReplies(String pid, Integer parentId);
}
