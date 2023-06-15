package com.flip.service;

import com.flip.domain.Response;
import com.flip.entity.Post;
import com.flip.entity.User;

import java.util.List;

public interface SearchService {
    Response<List<Post>> searchPostsByKey(String keyword);

    Response<List<User>> searchUsersByKey(String keyword);

    Response<String> insertAllPostToEs();

    Response<String> insertAllUserToEs();
}
