package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.entity.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    List<Post> getNewestPost();
}
