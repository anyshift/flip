package com.flip.controller;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.flip.common.Response;
import com.flip.domain.entity.Post;
import com.flip.domain.entity.User;
import com.flip.domain.enums.ResponseCode;
import com.flip.service.PostService;
import com.flip.service.SearchService;
import com.flip.service.UserService;
import com.flip.utils.elastic.ElasticPostUtils;
import com.flip.utils.elastic.ElasticUserUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private SearchService searchService;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @GetMapping("/post")
    public Response<Map<String, List<Post>>> searchPostsByKey(@RequestParam String keyword) {
        try {
            List<Hit<Post>> postHits = ElasticPostUtils.searchPostsByKey(elasticsearchClient, keyword);
            if (ObjectUtil.isNull(postHits)) {
                return Response.success("没有与关键词匹配的结果");
            } else {
                List<Post> searchedPosts = new ArrayList<>();
                postHits.forEach(postHit -> {
                    searchedPosts.add(postHit.source());
                });
                return Response.success("查询成功", Map.of("posts", searchedPosts));
            }
        } catch (IOException e) {
            return Response.failed(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "服务器错误，请稍后重试");
        }
    }

    @GetMapping("/user")
    public Response<Map<String, List<User>>> searchUsersByKey(@RequestParam String keyword) {
        try {
            List<Hit<User>> userHits = ElasticUserUtils.searchUsersByKey(elasticsearchClient, keyword);
            if (ObjectUtil.isNull(userHits)) {
                return Response.success("没有与关键词匹配的结果");
            } else {
                List<User> searchedUsers = new ArrayList<>();
                userHits.forEach(userHit -> {
                    searchedUsers.add(userHit.source());
                });
                return Response.success("查询成功", Map.of("users", searchedUsers));
            }
        } catch (IOException e) {
            return Response.failed(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "服务器错误，请稍后重试");
        }
    }

    @PostMapping("/addPosts")
    public Response<String> insertAllPostToEs() {
        boolean success;
        try {
            success = ElasticPostUtils.insertAllPostByBulkOperation(elasticsearchClient, postService);
        } catch (IOException e) {
            return Response.failed("插入数据失败");
        }
        if (success) {
            return Response.success("插入数据成功");
        } else return Response.failed("插入数据失败");
    }

    @PostMapping("/addUsers")
    public Response<String> insertAllUserToEs() {
        boolean success;
        try {
            success = ElasticUserUtils.insertAllUserByBulkOperation(elasticsearchClient, userService);
        } catch (IOException e) {
            return Response.failed("插入数据失败");
        }
        if (success) {
            return Response.success("插入数据成功");
        } else return Response.failed("插入数据失败");
    }
}
