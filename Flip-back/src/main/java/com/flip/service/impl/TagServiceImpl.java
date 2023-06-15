package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.Response;
import com.flip.entity.dto.LoggedUser;
import com.flip.entity.*;
import com.flip.mapper.*;
import com.flip.service.TagService;
import com.flip.utils.RedisKeyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final String tagsKey = RedisKeyUtils.getTagsKey();

    @Resource
    private TagMapper tagMapper;

    @Resource
    private TagOptionMapper tagOptionMapper;

    @Resource
    private PostTagMapper postTagMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Tag> redisTemplate;

    @Override
    public Response<Tag> addTag(Tag tag) {
        QueryWrapper<Tag> tagNameQueryWrapper = new QueryWrapper<>();
        tagNameQueryWrapper.eq("name", tag.getName());
        boolean tagNameExist = tagMapper.exists(tagNameQueryWrapper);
        if (tagNameExist) {
            return Response.error(400, "标签名已存在");
        }

        QueryWrapper<Tag> tagLabelQueryWrapper = new QueryWrapper<>();
        tagLabelQueryWrapper.eq("label", tag.getLabel());
        boolean tagLabelExist = tagMapper.exists(tagLabelQueryWrapper);
        if (tagLabelExist) {
            return Response.error(400, "标签英文标识已存在");
        }

        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tag.setCreator(loggedUser.getUser().getUid());

        int insertResult = tagMapper.insert(tag);
        if (insertResult > 0) {
            QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
            tagOptionQueryWrapper.eq("id", tag.getOptionId());
            TagOption tagOption = tagOptionMapper.selectOne(tagOptionQueryWrapper);
            tag.setTagOption(tagOption);

            redisTemplate.delete(tagsKey);
            getAllTag();
            return Response.success(200, "标签创建成功", Map.of("newTag", tag));
        }
        return Response.error(400, "标签创建失败");
    }

    @Override
    public Response<List<Tag>> getAllTag() {
        List<Tag> tags;
        Boolean hasKey = redisTemplate.hasKey(tagsKey);
        if (Boolean.TRUE.equals(hasKey)) {
            tags = redisTemplate.opsForList().range(tagsKey, 0, -1);
            if (ObjectUtil.isNotNull(tags)) {
                return Response.success(200, Map.of("tags", tags));
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

        return Response.success(200, Map.of("tags", allTag));
    }

    @Override
    public Response<Object> getTagAndPosts(String tagLabel, Integer currentPage) {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("label", tagLabel);
        Tag tag = tagMapper.selectOne(tagQueryWrapper);

        if (ObjectUtil.isNull(tag)) {
            return Response.error(400, "请检查URL是否正确");
        }

        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("id", tag.getOptionId());
        TagOption tagOption = tagOptionMapper.selectOne(tagOptionQueryWrapper);
        tag.setTagOption(tagOption);

        int sizePerPage = 30;
        IPage<PostTag> page = new Page<>(currentPage, sizePerPage);

        QueryWrapper<PostTag> postTagQueryWrapper = new QueryWrapper<>();
        postTagQueryWrapper.eq("tag_label", tagLabel);
        IPage<PostTag> postTagIPage = postTagMapper.selectPage(page, postTagQueryWrapper);
        List<PostTag> postTags = postTagIPage.getRecords();

        List<Post> posts = new ArrayList<>();
        postTags.forEach(postTag -> {
            QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
            postQueryWrapper.eq("id", postTag.getPid());
            Post post = postMapper.selectOne(postQueryWrapper);

            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("uid", post.getUid()).select("username, nickname, avatar");
            User user = userMapper.selectOne(userQueryWrapper);
            post.setAuthor(user.getUsername());
            post.setNickname(user.getNickname());
            post.setAvatar(user.getAvatar());

            posts.add(post);
        });

        Map<String, Object> map = new HashMap<>();
        map.put("tag", tag);
        map.put("posts", posts);
        map.put("totalItems", Integer.parseInt(String.valueOf(page.getTotal())));
        map.put("sizePerPage", Integer.parseInt(String.valueOf(page.getSize())));
        map.put("totalPages", Integer.parseInt(String.valueOf(page.getPages())));
        map.put("currentPage", Integer.parseInt(String.valueOf(page.getCurrent())));

        return Response.success(200, map);
    }

    @Override
    public Response<List<Tag>> updateTag(Tag tag) {
        int updateResult = tagMapper.updateById(tag);;
        if (updateResult > 0) {
            redisTemplate.delete(tagsKey);
            return getAllTag(); //更新成功后返回全部标签
        } else return Response.error(400, "更新标签失败");
    }

    @Override
    public Response<List<Tag>> deleteTag(Tag tag) {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("label", tag.getLabel());
        if (!tagMapper.exists(tagQueryWrapper)) {
            return Response.error(400, "找不到该标签");
        }

        QueryWrapper<PostTag> postTagExistQueryWrapper = new QueryWrapper<>();
        postTagExistQueryWrapper.eq("tag_label", tag.getLabel());
        if (postTagMapper.exists(postTagExistQueryWrapper)) {
            return Response.error(400, "该标签已存在主题绑定");
        }

        int deleteResult = tagMapper.delete(tagQueryWrapper);
        if (deleteResult > 0) {
            redisTemplate.delete(tagsKey);
            return getAllTag();
        } else return Response.error(400, "删除失败");
    }

    @Override
    @Transactional
    public Response<List<Tag>> forceDeleteTag(Tag tag) {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("label", tag.getLabel());
        if (!tagMapper.exists(tagQueryWrapper)) {
            return Response.error(400, "找不到该标签");
        }

        /* 检查是否存在标签类型的label为other的类型，没有则创建 */
        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("label", "other");
        TagOption tagOption = tagOptionMapper.selectOne(tagOptionQueryWrapper);
        if (ObjectUtil.isNull(tagOption)) {
            tagOption = new TagOption(null, "其它", "other");
            tagOptionMapper.insert(tagOption);
        }

        /* 检查是否存在标签的label为unclassified的标签，没有则创建 */
        QueryWrapper<Tag> unClassifiedTagQueryWrapper = new QueryWrapper<>();
        unClassifiedTagQueryWrapper.eq("label", "unclassified");
        if (!tagMapper.exists(unClassifiedTagQueryWrapper)) {
            LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Tag unclassifiedTag = new Tag(tagOption.getId(), "未分类", "unclassified", "", "");
            unclassifiedTag.setCreator(loggedUser.getUser().getUid());
            tagMapper.insert(unclassifiedTag);
        }

        /* 更换所有已绑定待删除标签的帖子的标签为未分类 */
        UpdateWrapper<PostTag> postTagUpdateWrapper = new UpdateWrapper<>();
        postTagUpdateWrapper.eq("tag_label", tag.getLabel()).set("tag_label", "unclassified");
        postTagMapper.update(null, postTagUpdateWrapper);

        /* 删除当前标签 */
        int deleteResult = tagMapper.delete(tagQueryWrapper);
        if (deleteResult > 0) {
            redisTemplate.delete(tagsKey);
            return getAllTag();
        } else return Response.error(400, "强制删除标签失败");
    }
}
