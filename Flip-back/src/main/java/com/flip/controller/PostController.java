package com.flip.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.flip.common.Response;
import com.flip.domain.dto.LoggedUser;
import com.flip.domain.entity.*;
import com.flip.domain.enums.ResponseCode;
import com.flip.service.PostService;
import com.flip.service.SensitiveWordService;
import com.flip.service.TagService;
import com.flip.service.UserService;
import com.flip.utils.LoggedUserUtils;
import com.flip.utils.SensitiveWordUtils;
import com.flip.utils.elastic.ElasticPostUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private UserService userService;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private TagService tagService;

    /**
     * 发布帖子
     * @param post 帖子
     * @return result
     */
    @PostMapping("/post")
    public Response<Map<String, String>> doPost(@RequestBody @Valid Post post) {
        LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
        post.setUid(loggedUser.getUser().getUid());

        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String postTitle = SensitiveWordUtils.stringSearchEx2Filter(post.getTitle(), sensitiveWords);
        String postContent = SensitiveWordUtils.stringSearchEx2Filter(post.getContent(), sensitiveWords);

        post.setTitle(postTitle);
        post.setContent(postContent);

        boolean saved = postService.save(post);
        if (saved) {
            post.getTags().forEach(tag -> {
                postService.savePostTag(new PostTag(null, post.getId(), tag.getLabel(), loggedUser.getUser().getUid(), null));
            });

            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.select("username, nickname, avatar").eq("uid", post.getUid());
            User user = userService.getOne(userQueryWrapper);
            post.setAuthor(user.getUsername());
            post.setNickname(user.getNickname());
            post.setAvatar(user.getAvatar());

            try {
                ElasticPostUtils.insertPostToEs(elasticsearchClient, post);
            } catch (IOException e) {
                return Response.success("帖子发布成功", Map.of("postId", String.valueOf(post.getId())));
            }

            return Response.success("帖子发布成功", Map.of("postId", String.valueOf(post.getId())));
        }

        return Response.failed("帖子发布失败");
    }

    /**
     * 修改帖子内容
     * @param post 帖子
     * @return
     */
    @PutMapping("/postContent")
    public Response<Map<String, String>> doEditContent(@RequestBody @Valid Post post) {

        if (post.getContent().length() >= 65536) {
            return Response.failed("内容长度超限");
        }

        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String postContent = SensitiveWordUtils.stringSearchEx2Filter(post.getContent(), sensitiveWords);

        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", post.getId()).set("content", postContent);
        boolean updated = postService.update(null, updateWrapper);
        if (updated) {
            try {
                post.setContent(HtmlUtil.cleanHtmlTag(postContent));
                ElasticPostUtils.updatePostContentInEs(elasticsearchClient, post);
            } catch (IOException e) {
                return Response.success("修改内容成功", Map.of("content", postContent));
            }
            return Response.success("修改内容成功", Map.of("content", postContent));
        }

        return Response.failed("修改内容失败");
    }

    @PutMapping("/postTitle")
    public Response<Map<String, String>> doEditTitle(@RequestParam String pid, @RequestParam String title) {
        if (title.length() > 100) {
            return Response.failed("标题长度超限");
        }

        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String postTitle = SensitiveWordUtils.stringSearchEx2Filter(title, sensitiveWords);

        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", pid).set("title", HtmlUtil.escape(postTitle));
        boolean updated = postService.update(null, updateWrapper);
        if (updated) {
            try {
                Post post = new Post();
                post.setId(Long.parseLong(pid));
                post.setTitle(postTitle);
                ElasticPostUtils.updatePostTitleInEs(elasticsearchClient, post);
            } catch (IOException e) {
                return Response.success("修改标题成功", Map.of("title", postTitle));
            }
            return Response.success("修改标题成功", Map.of("title", postTitle));
        }

        return Response.failed("修改标题失败");
    }

    /**
     * 获取各种帖子列表
     * @param node 帖子列表类型
     * @param page 帖子所处列表的当前页码
     * @return result
     */
    @GetMapping("/list")
    public Response<Map<String, Object>> getPostList(@RequestParam String node, @RequestParam(required = false) Integer page) {

        if ("latestPost".equalsIgnoreCase(node)) return Response.success("", postService.getLatestPostList(page));

        if ("allPost".equalsIgnoreCase(node)) return Response.success("", postService.getALlPostList(page));

        if ("hotPost".equalsIgnoreCase(node)) return Response.success("", postService.getHotPostList(page));

        return null;
    }

    /**
     * 获取帖子详情
     * @param pid 帖子ID
     * @return result
     */
    @GetMapping("/postInfo")
    public Response<Map<String, Object>> getPostInfo(@RequestParam String pid) {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("id", pid);
        Post post = postService.getOne(postQueryWrapper);

        if (ObjectUtil.isNull(post)) {
            return Response.failed(ResponseCode.NOT_FOUND.getCode(), "请检查URL是否输入正确");
        }

        List<PostTag> postTags = postService.getPostTags(pid);
        List<Tag> tags = new ArrayList<>();
        postTags.forEach(tagPost -> {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("label", tagPost.getTagLabel());
            Tag tag = tagService.getOne(tagQueryWrapper);
            tags.add(tag);
        });
        post.setTags(tags);

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id, username, nickname, avatar").eq("uid", post.getUid());
        User user = userService.getOne(userQueryWrapper);
        Role role = userService.loadUserRoleByUid(String.valueOf(post.getUid()));

        Map<String, Object> map = new HashMap<>();
        map.put("post", post);
        map.put("id", user.getId());
        map.put("uid", post.getUid());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("role", role);

        return Response.success("获取帖子详情成功", map);
    }

    @PutMapping("/tagOfPost")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<Map<String, List<Tag>>> changeTagOfPost(@RequestBody Post post) {
        LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
        Long pid = post.getId();

        //更新前，把原有的标签记录删掉
        postService.deletePostTags(pid);

        List<Tag> newTags = new ArrayList<>();
        post.getTags().forEach(tag -> {
            postService.addTagToPost(new PostTag(null, pid, tag.getLabel(), loggedUser.getUser().getUid(), null));

            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("label", tag.getLabel()).select("label, name");
            newTags.add(tagService.getOne(tagQueryWrapper));
        });

        post.setTags(newTags);
        try {
            ElasticPostUtils.updatePostTagsInEs(elasticsearchClient, post);
        } catch (Exception e) {
            return Response.success("已更新标签", Map.of("newTags", newTags));
        }
        return Response.success("已更新标签", Map.of("newTags", newTags));
    }
}
