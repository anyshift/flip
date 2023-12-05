package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.entity.PostTag;
import com.flip.domain.entity.Tag;
import com.flip.domain.entity.TagOption;
import com.flip.mapper.PostTagMapper;
import com.flip.mapper.TagMapper;
import com.flip.mapper.TagOptionMapper;
import com.flip.service.TagService;
import com.flip.utils.RedisKeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final String tagsKey = RedisKeyUtils.getTagsKey();

    private final TagMapper tagMapper;

    private final TagOptionMapper tagOptionMapper;

    private final PostTagMapper postTagMapper;

    private final RedisTemplate<String, Tag> redisTemplate;

    @Override
    public List<Tag> getAllTag() {
        List<Tag> tags;
        Boolean hasKey = redisTemplate.hasKey(tagsKey);
        if (Boolean.TRUE.equals(hasKey)) {
            tags = redisTemplate.opsForList().range(tagsKey, 0, -1);
            if (ObjectUtil.isNotNull(tags)) {
                return tags;
            }
        }

        tags = tagMapper.selectList(null);
        List<Tag> allTag = new ArrayList<>();

        tags.forEach(tag -> {
            QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
            tagOptionQueryWrapper.eq("id", tag.getOptionId());
            TagOption tagOption = tagOptionMapper.selectOne(tagOptionQueryWrapper);
            tag.setTagOption(tagOption);
            allTag.add(tag);
            redisTemplate.opsForList().rightPush(tagsKey, tag);
        });
        return allTag;
    }

    @Override
    public IPage<PostTag> selectPostTagPage(IPage<PostTag> page, QueryWrapper<PostTag> queryWrapper) {
        return postTagMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Boolean isTagUsed(Tag tag) {
        QueryWrapper<PostTag> postTagExistQueryWrapper = new QueryWrapper<>();
        postTagExistQueryWrapper.eq("tag_label", tag.getLabel());
        return postTagMapper.exists(postTagExistQueryWrapper);
    }

    @Override
    public void updatePostTag(UpdateWrapper<PostTag> updateWrapper) {
        postTagMapper.update(updateWrapper);
    }

}
