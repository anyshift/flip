package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.entity.TagOption;
import com.flip.mapper.TagOptionMapper;
import com.flip.service.TagOptionService;
import com.flip.service.TagService;
import com.flip.utils.RedisKeyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TagOptionServiceImpl extends ServiceImpl<TagOptionMapper, TagOption> implements TagOptionService {

    private final String tagOptionsKey = RedisKeyUtils.getTagOptionsKey();

    @Resource
    private TagOptionMapper tagOptionMapper;

    @Resource
    private TagService tagService;

    @Resource
    private RedisTemplate<String, TagOption> redisTemplate;

    @Override
    public List<TagOption> getAllTagOptions() {
        List<TagOption> tagOptions;
        Boolean hasKey = redisTemplate.hasKey(tagOptionsKey);
        if (Boolean.TRUE.equals(hasKey)) {
            tagOptions = redisTemplate.opsForList().range(tagOptionsKey, 0, -1);
            if (ObjectUtil.isNotNull(tagOptions)) {
                return tagOptions;
            }
        }

        tagOptions = tagOptionMapper.selectList(null);
        tagOptions.forEach(tagOption -> {
            redisTemplate.opsForList().rightPush(tagOptionsKey, tagOption);
        });

        return tagOptions;
    }

    public Map<String, Object> getTagsAndOptions() {
        return Map.of("tags", tagService.getAllTag(), "tagOptions", getAllTagOptions());
    }
}
