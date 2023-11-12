package com.flip.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.entity.PostTag;
import com.flip.domain.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    List<Tag> getAllTag();

    IPage<PostTag> selectPostTagPage(IPage<PostTag> page, QueryWrapper<PostTag> queryWrapper);

    Boolean isTagUsed(Tag tag);

    void updatePostTag(UpdateWrapper<PostTag> updateWrapper);

}
