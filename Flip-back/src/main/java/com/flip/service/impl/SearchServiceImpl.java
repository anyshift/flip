package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.flip.domain.Response;
import com.flip.entity.Post;
import com.flip.entity.User;
import com.flip.service.PostService;
import com.flip.service.SearchService;
import com.flip.service.UserService;
import com.flip.utils.elastic.ElasticPostUtils;
import com.flip.utils.elastic.ElasticUserUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @Override
    public Response<List<Post>> searchPostsByKey(String keyword) {
        try {
            List<Hit<Post>> postHits = ElasticPostUtils.searchPostsByKey(elasticsearchClient, keyword);
            if (ObjectUtil.isNull(postHits)) {
                return Response.success(200, "没有与关键词匹配的结果");
            } else {
                List<Post> searchedPosts = new ArrayList<>();
                postHits.forEach(postHit -> {
                    searchedPosts.add(postHit.source());
                });
                return Response.success(200, Map.of("posts", searchedPosts));
            }
        } catch (IOException e) {
            return Response.error(500, "服务器错误，请稍后重试");
        }
    }

    @Override
    public Response<List<User>> searchUsersByKey(String keyword) {
        try {
            List<Hit<User>> userHits = ElasticUserUtils.searchUsersByKey(elasticsearchClient, keyword);
            if (ObjectUtil.isNull(userHits)) {
                return Response.success(200, "没有与关键词匹配的结果");
            } else {
                List<User> searchedUsers = new ArrayList<>();
                userHits.forEach(userHit -> {
                    searchedUsers.add(userHit.source());
                });
                return Response.success(200, Map.of("users", searchedUsers));
            }
        } catch (IOException e) {
            return Response.error(500, "服务器错误，请稍后重试");
        }
    }

    @Override
    public Response<String> insertAllPostToEs() {
        boolean success = false;
        try {
            success = ElasticPostUtils.insertAllPostByBulkOperation(elasticsearchClient, postService);
        } catch (IOException e) {
            return Response.error(400, "插入失败");
        }
        if (success) {
            return Response.success(200, "插入成功");
        } else return Response.error(400, "插入失败");
    }

    @Override
    public Response<String> insertAllUserToEs() {
        boolean success = false;
        try {
            success = ElasticUserUtils.insertAllUserByBulkOperation(elasticsearchClient, userService);
        } catch (IOException e) {
            return Response.error(400, "插入失败");
        }
        if (success) {
            return Response.success(200, "插入成功");
        } else return Response.error(400, "插入失败");
    }
}
