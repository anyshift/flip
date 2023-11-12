package com.flip.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flip.common.Response;
import com.flip.domain.dto.LoggedUser;
import com.flip.domain.entity.*;
import com.flip.service.PostService;
import com.flip.service.TagOptionService;
import com.flip.service.TagService;
import com.flip.service.UserService;
import com.flip.utils.LoggedUserUtils;
import com.flip.utils.RedisKeyUtils;
import com.flip.validation.VG;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TagController {

    private final String tagsKey = RedisKeyUtils.getTagsKey();

    private final String tagOptionsKey = RedisKeyUtils.getTagOptionsKey();

    @Resource
    private TagService tagService;

    @Resource
    private TagOptionService tagOptionService;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/tags")
    public Response<Map<String, List<Tag>>> getAllTag() {
        return Response.success("获取标签成功", Map.of("tags", tagService.getAllTag()));
    }

    @GetMapping("/tagsAndOptions")
    public Response<Map<String, Object>> getTagsAndOptions() {
        return Response.success("获取标签和标签选项成功", tagOptionService.getTagsAndOptions());
    }

    @GetMapping("/tag")
    public Response<Map<String, Object>> getTag(@RequestParam("label") String tagLabel, @RequestParam("page") Integer currentPage) {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("label", tagLabel);
        Tag tag = tagService.getOne(tagQueryWrapper);

        if (ObjectUtil.isNull(tag)) {
            return null;
        }

        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("id", tag.getOptionId());
        TagOption tagOption = tagOptionService.getOne(tagOptionQueryWrapper);
        tag.setTagOption(tagOption);

        int sizePerPage = 30;
        IPage<PostTag> page = new Page<>(currentPage, sizePerPage);

        QueryWrapper<PostTag> postTagQueryWrapper = new QueryWrapper<>();
        postTagQueryWrapper.eq("tag_label", tagLabel);
        IPage<PostTag> postTagIPage = tagService.selectPostTagPage(page, postTagQueryWrapper);
        List<PostTag> postTags = postTagIPage.getRecords();

        List<Post> posts = new ArrayList<>();
        postTags.forEach(postTag -> {
            QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
            postQueryWrapper.eq("id", postTag.getPid());
            Post post = postService.getOne(postQueryWrapper);

            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("uid", post.getUid()).select("username, nickname, avatar");
            User user = userService.getOne(userQueryWrapper);
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

        return Response.success("获取标签成功", map);
    }

    @PostMapping("/sys-ctrl/tag")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<Map<String, Tag>> addTag(@Validated(VG.class) @RequestBody Tag tag) {
        QueryWrapper<Tag> tagNameQueryWrapper = new QueryWrapper<>();
        tagNameQueryWrapper.eq("name", tag.getName());
        boolean tagNameExist = tagService.exists(tagNameQueryWrapper);
        if (tagNameExist) {
            return Response.failed("标签名已存在");
        }

        QueryWrapper<Tag> tagLabelQueryWrapper = new QueryWrapper<>();
        tagLabelQueryWrapper.eq("label", tag.getLabel());
        boolean tagLabelExist = tagService.exists(tagLabelQueryWrapper);
        if (tagLabelExist) {
            return Response.failed(400, "标签英文标识已存在");
        }

        LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
        tag.setCreator(loggedUser.getUser().getUid());

        boolean saved = tagService.save(tag);
        if (saved) {
            QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
            tagOptionQueryWrapper.eq("id", tag.getOptionId());
            TagOption tagOption = tagOptionService.getOne(tagOptionQueryWrapper);
            tag.setTagOption(tagOption);

            redisTemplate.delete(tagsKey);
            getAllTag();
            return Response.success("标签创建成功", Map.of("newTag", tag));
        }
        return Response.failed("标签创建失败");
    }

    @PutMapping("/sys-ctrl/tag")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<List<Tag>> updateTag(@Validated(VG.class) @RequestBody Tag tag) {
        boolean updated = tagService.update(tag, null);
        if (updated) {
            redisTemplate.delete(tagsKey);
            return Response.success("更新标签成功", tagService.getAllTag());
        } else return Response.failed("更新标签失败");
    }

    @DeleteMapping("/sys-ctrl/tag")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<List<Tag>> deleteTag(@RequestBody Tag tag) {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("label", tag.getLabel());
        if (!tagService.exists(tagQueryWrapper)) {
            return Response.failed("找不到该标签");
        }

        if (tagService.isTagUsed(tag)) {
            return Response.failed("该标签已存在主题绑定");
        }

        boolean deleted = tagService.remove(tagQueryWrapper);
        if (deleted) {
            redisTemplate.delete(tagsKey);
            return Response.success("删除标签成功", tagService.getAllTag());
        } else return Response.failed("删除失败");
    }

    @DeleteMapping("/sys-ctrl/tagForce")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<List<Tag>> forceDeleteTag(@RequestBody Tag tag) {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("label", tag.getLabel());
        if (!tagService.exists(tagQueryWrapper)) {
            return Response.failed("找不到该标签");
        }

        /* 检查是否存在标签类型的label为other的类型，没有则创建 */
        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("label", "other");
        TagOption tagOption = tagOptionService.getOne(tagOptionQueryWrapper);
        if (ObjectUtil.isNull(tagOption)) {
            tagOption = new TagOption(null, "其它", "other");
            tagOptionService.save(tagOption);
        }

        /* 检查是否存在标签的label为unclassified的标签，没有则创建 */
        QueryWrapper<Tag> unClassifiedTagQueryWrapper = new QueryWrapper<>();
        unClassifiedTagQueryWrapper.eq("label", "unclassified");
        if (!tagService.exists(unClassifiedTagQueryWrapper)) {
            LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
            Tag unclassifiedTag = new Tag(tagOption.getId(), "未分类", "unclassified", "", "");
            unclassifiedTag.setCreator(loggedUser.getUser().getUid());
            tagService.save(unclassifiedTag);
        }

        /* 更换所有已绑定待删除标签的帖子的标签为未分类 */
        UpdateWrapper<PostTag> postTagUpdateWrapper = new UpdateWrapper<>();
        postTagUpdateWrapper.eq("tag_label", tag.getLabel()).set("tag_label", "unclassified");
        tagService.updatePostTag(postTagUpdateWrapper);

        /* 删除当前标签 */
        boolean deleted = tagService.remove(tagQueryWrapper);
        if (deleted) {
            redisTemplate.delete(tagsKey);
            return Response.success("强制删除标签成功", tagService.getAllTag());
        } else return Response.failed("强制删除标签失败");
    }

    @GetMapping("/sys-ctrl/tagOption")
    public Response<List<TagOption>> getAllTagOptions() {
        return Response.success("获取标签选项成功", tagOptionService.getAllTagOptions());
    }

    @PostMapping("/sys-ctrl/tagOption")
    public Response<Map<String, TagOption>> addTagOption(@RequestBody TagOption tagOption) {
        QueryWrapper<TagOption> tagOptionNameQueryWrapper = new QueryWrapper<>();
        tagOptionNameQueryWrapper.eq("name", tagOption.getName());
        boolean tagOptionNameExists = tagOptionService.exists(tagOptionNameQueryWrapper);
        if (tagOptionNameExists) {
            return Response.failed("类型显示名称已存在");
        }

        QueryWrapper<TagOption> tagOptionLabelQueryWrapper = new QueryWrapper<>();
        tagOptionLabelQueryWrapper.eq("label", tagOption.getLabel());
        boolean tagOptionLabelExists = tagOptionService.exists(tagOptionLabelQueryWrapper);
        if (tagOptionLabelExists) {
            return Response.failed("类型英文标识已存在");
        }

        boolean saved = tagOptionService.save(tagOption);
        if (saved) {
            redisTemplate.opsForList().rightPushIfPresent(tagOptionsKey, tagOption);
            return Response.success("添加标签成功", Map.of("newTagOption", tagOption));
        } else return Response.failed("添加标签类型失败");
    }

    @PutMapping("/sys-ctrl/tagOption")
    public Response<Map<String, TagOption>> updateTagOption(@Validated(VG.class) @RequestBody TagOption tagOption) {
        boolean updated = tagOptionService.updateById(tagOption);
        if (updated) {
            redisTemplate.delete(tagOptionsKey);
            getAllTagOptions();
            return Response.success("更新类型成功", Map.of("newOption", tagOption));
        } else return Response.failed("更新类型失败");
    }

    @DeleteMapping("/sys-ctrl/tagOption")
    public Response<List<TagOption>> deleteTagOption(@RequestBody TagOption tagOption) {
        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("label", tagOption.getLabel());
        if (!tagOptionService.exists(tagOptionQueryWrapper)) {
            return Response.failed("找不到该标签类型");
        }

        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("option_id", tagOption.getId());
        if (tagService.exists(tagQueryWrapper)) {
            return Response.failed("当前类型已存在标签绑定");
        }

        boolean deleted = tagOptionService.remove(tagOptionQueryWrapper);
        if (deleted) {
            redisTemplate.delete(tagOptionsKey);
            return getAllTagOptions();
        } return Response.failed("删除标签类型失败");
    }

    @DeleteMapping("/sys-ctrl/tagOptionForce")
    public Response<Object> forceDeleteTagOption(@RequestBody TagOption tagOption) {
        QueryWrapper<TagOption> tagOptionQueryWrapper = new QueryWrapper<>();
        tagOptionQueryWrapper.eq("label", tagOption.getLabel());
        if (!tagOptionService.exists(tagOptionQueryWrapper)) {
            return Response.failed("找不到该标签类型");
        }

        /* 检查是否存在标签类型的label为other的类型，没有则创建 */
        QueryWrapper<TagOption> newTagOptionQueryWrapper = new QueryWrapper<>();
        newTagOptionQueryWrapper.eq("label", "other");
        TagOption newTagOption = tagOptionService.getOne(newTagOptionQueryWrapper);
        if (ObjectUtil.isNull(newTagOption)) {
            newTagOption = new TagOption(null, "其它", "other");
            tagOptionService.save(newTagOption);
        }

        /* 检查是否存在标签的label为unclassified的标签，没有则创建 */
        QueryWrapper<Tag> unClassifiedTagQueryWrapper = new QueryWrapper<>();
        unClassifiedTagQueryWrapper.eq("label", "unclassified");
        if (!tagService.exists(unClassifiedTagQueryWrapper)) {
            LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
            Tag unclassifiedTag = new Tag(newTagOption.getId(), "未分类", "unclassified", "", "");
            unclassifiedTag.setCreator(loggedUser.getUser().getUid());
            tagService.save(unclassifiedTag);
        }

        /* 将待删除的标签类型下的所有标签指向「其它」类型 */
        UpdateWrapper<Tag> tagUpdateWrapper = new UpdateWrapper<>();
        tagUpdateWrapper.eq("option_id", tagOption.getId()).set("option_id", newTagOption.getId());
        tagService.update(null, tagUpdateWrapper);

        boolean deleted = tagOptionService.remove(tagOptionQueryWrapper);
        if (deleted) {
            redisTemplate.delete(tagOptionsKey);
            return Response.success("强制删除标签类型成功", tagOptionService.getTagsAndOptions());
        } else return Response.failed("强制删除标签类型失败");
    }
}
