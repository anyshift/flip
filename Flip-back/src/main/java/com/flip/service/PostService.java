package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.entity.Post;
import com.flip.domain.entity.PostTag;

import java.util.List;
import java.util.Map;

public interface PostService extends IService<Post> {

    void savePostTag(PostTag postTag);

    Map<String, Object> getLatestPostList(Integer currentPage);

    Map<String, Object> getALlPostList(Integer page);

    Map<String, Object> getHotPostList(Integer page);

    void deletePostTags(Long pid);

    void addTagToPost(PostTag postTag);

    List<PostTag> getPostTags(String pid);

}
