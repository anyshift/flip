package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.Response;
import com.flip.entity.Post;
import com.flip.entity.Tag;

import java.util.List;

public interface PostService extends IService<Post> {
    Response<String> doPost(Post post);

    Response<Object> getLatestPostList(Integer currentPage);

    Response<Object> getPostInfo(String pid);

    Response<Object> doEditContent(Post post);

    Response<Object> doEditTitle(String pid, String title);

    Response<Object> getALlPostList(Integer page);

    Response<Object> getHotPostList(Integer page);

    Response<List<Tag>> changeTagOfPost(Post post);
}
