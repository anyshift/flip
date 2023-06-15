package com.flip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flip.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    List<String> loadPostsIdByTagName(@Param("tagName") String tagName, @Param("start") Integer start, @Param("size") Integer size);

    Integer loadPostsCountByTagName(@Param("tagName") String tagName);
}
