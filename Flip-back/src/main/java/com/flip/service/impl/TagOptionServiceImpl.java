package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.Response;
import com.flip.entity.dto.LoggedUser;
import com.flip.entity.Tag;
import com.flip.entity.TagOption;
import com.flip.mapper.PostTagMapper;
import com.flip.mapper.TagMapper;
import com.flip.mapper.TagOptionMapper;
import com.flip.service.TagOptionService;
import com.flip.service.TagService;
import com.flip.utils.RedisKeyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TagOptionServiceImpl extends ServiceImpl<TagOptionMapper, TagOption> implements TagOptionService {

    private final String tagOptionsKey = RedisKeyUtils.getTagOptionsKey();

    @Resource
    private TagOptionMapper tagOptionMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private PostTagMapper postTagMapper;

    @Resource
    private TagService tagService;

    @Resource
    private RedisTemplate<String, TagOption> redisTemplate;

    @Override
    public Response<TagOption> addTagOption(TagOption tagOption) {
        QueryWrapper<TagOption> tagOptionNameQueryWrapper = new QueryWrapper<>();
        tagOptionNameQueryWrapper.eq("name", tagOption.getName());
        boolean tagOptionNameExists = tagOptionMapper.exists(tagOptionNameQueryWrapper);
        if (tagOptionNameExists) {
            return Response.error(400, "类型显示名称已存在");
        }

        QueryWrapper<TagOption> tagOptionLabelQueryWrapper = new QueryWrapper<>();
        tagOptionLabelQueryWrapper.eq("label", tagOption.getLabel());
        boolean tagOptionLabelExists = tagOptionMapper.exists(tagOptionLabelQueryWrapper);
        if (tagOptionLabelExists) {
            return Response.error(400, "类型英文标识已存在");
        }

        int insertResult = tagOptionMapper.insert(tagOption);
        if (insertResult > 0) {
            redisTemplate.opsForList().rightPushIfPresent(tagOptionsKey, tagOption);
            return Response.success(200, "添加标签成功", Map.of("newTagOption", tagOption));
        } else return Response.error(400, "添加标签类型失败");
    }

    @Override
    public Response<List<TagOption>> getAllTagOptions() {
        List<TagOption> tagOptions;
        Boolean hasKey = redisTemplate.hasKey(tagOptionsKey);
        if (Boolean.TRUE.equals(hasKey)) {
            tagOptions = redisTemplate.opsForList().range(tagOptionsKey, 0, -1);
            if (ObjectUtil.isNotNull(tagOptions)) {
                return Response.success(200, Map.of("tagOptions", tagOptions));
            }
        }

        tagOptions = tagOptionMapper.selectList(null);
        tagOptions.forEach(tagOption -> {
            redisTemplate.opsForList().rightPush(tagOptionsKey, tagOption);
        });

        return Response.success(200, Map.of("tagOptions", tagOptions));
    }

    @Override
    public Response<TagOption> updateTagOption(TagOption tagOption) {
        int updateResult = tagOptionMapper.updateById(tagOption);
        if (updateResult > 0) {
            redisTemplate.delete(tagOptionsKey);
            getAllTagOptions();
            return Response.success(200, "更新类型成功", Map.of("newOption", tagOption));
        } else return Response.error(400, "更新类型失败");
    }

    @Override
    public Response<List<TagOption>> deleteTagOption(TagOption tagOption) {
        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("label", tagOption.getLabel());
        if (!tagOptionMapper.exists(tagOptionQueryWrapper)) {
            return Response.error(400, "找不到该标签类型");
        }

        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("option_id", tagOption.getId());
        if (tagMapper.exists(tagQueryWrapper)) {
            return Response.error(400, "当前类型已存在标签绑定");
        }

        int deleteResult = tagOptionMapper.delete(tagOptionQueryWrapper);
        if (deleteResult > 0) {
            redisTemplate.delete(tagOptionsKey);
            return getAllTagOptions();
        } return Response.error(400, "删除标签类型失败");
    }

    @Override
    @Transactional
    public Response<Object> forceDeleteTagOption(TagOption tagOption) {
        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("label", tagOption.getLabel());
        if (!tagOptionMapper.exists(tagOptionQueryWrapper)) {
            return Response.error(400, "找不到该标签类型");
        }

        /* 检查是否存在标签类型的label为other的类型，没有则创建 */
        QueryWrapper<TagOption> newTagOptionQueryWrapper = new QueryWrapper<>();
        newTagOptionQueryWrapper.eq("label", "other");
        TagOption newTagOption = tagOptionMapper.selectOne(newTagOptionQueryWrapper);
        if (ObjectUtil.isNull(newTagOption)) {
            newTagOption = new TagOption(null, "其它", "other");
            tagOptionMapper.insert(newTagOption);
        }

        /* 检查是否存在标签的label为unclassified的标签，没有则创建 */
        QueryWrapper<Tag> unClassifiedTagQueryWrapper = new QueryWrapper<>();
        unClassifiedTagQueryWrapper.eq("label", "unclassified");
        if (!tagMapper.exists(unClassifiedTagQueryWrapper)) {
            LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Tag unclassifiedTag = new Tag(newTagOption.getId(), "未分类", "unclassified", "", "");
            unclassifiedTag.setCreator(loggedUser.getUser().getUid());
            tagMapper.insert(unclassifiedTag);
        }

        /* 将待删除的标签类型下的所有标签指向「其它」类型 */
        UpdateWrapper<Tag> tagUpdateWrapper = new UpdateWrapper<>();
        tagUpdateWrapper.eq("option_id", tagOption.getId()).set("option_id", newTagOption.getId());
        tagMapper.update(null, tagUpdateWrapper);

        int deleteResult = tagOptionMapper.delete(tagOptionQueryWrapper);
        if (deleteResult > 0) {
            redisTemplate.delete(tagOptionsKey);
            return getTagsAndOptions();
        } else return Response.error(400, "删除标签类型失败");
    }

    public Response<Object> getTagsAndOptions() {
        return Response.success(200, Map.of("tags", tagService.getAllTag().getData().get("tags"),
                "tagOptions", getAllTagOptions().getData().get("tagOptions")));
    }
}
