package com.flip.controller.common;

import com.flip.domain.Response;
import com.flip.entity.Post;
import com.flip.entity.Tag;
import com.flip.service.PostService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 发布帖子
     * @param post 帖子
     * @return
     */
    @PostMapping("/post")
    public Response<String> doPost(@RequestBody @Valid Post post) {
        return postService.doPost(post);
    }

    /**
     * 修改帖子内容
     * @param post 帖子
     * @return
     */
    @PutMapping("/postContent")
    public Response<Object> doEditContent(@RequestBody @Valid Post post) {
        return postService.doEditContent(post);
    }

    @PutMapping("/postTitle")
    public Response<Object> doEditTitle(@RequestParam String pid, @RequestParam String title) {
        return postService.doEditTitle(pid, title);
    }

    /**
     * 获取各种帖子列表
     * @param node 帖子列表类型
     * @param page 帖子所处列表的当前页码
     * @return
     */
    @GetMapping("/list")
    public Response<Object> getPostList(@RequestParam String node, @RequestParam(required = false) Integer page) {

        if ("latestPost".equalsIgnoreCase(node)) return postService.getLatestPostList(page);

        if ("allPost".equalsIgnoreCase(node)) return postService.getALlPostList(page);

        if ("hotPost".equalsIgnoreCase(node)) return postService.getHotPostList(page);

        return null;
    }

    /**
     * 获取帖子详情
     * @param pid 帖子ID
     * @return
     */
    @GetMapping("/postInfo")
    public Response<Object> getPostInfo(@RequestParam String pid) {
        return postService.getPostInfo(pid);
    }

    @PutMapping("/tagOfPost")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<List<Tag>> changeTagOfPost(@RequestBody Post post) {
        return postService.changeTagOfPost(post);
    }
}
