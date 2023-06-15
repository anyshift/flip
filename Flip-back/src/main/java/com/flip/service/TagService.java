package com.flip.service;

import com.flip.domain.Response;
import com.flip.entity.Tag;

import java.util.List;

public interface TagService {
    Response<Tag> addTag(Tag tag);

    Response<List<Tag>> getAllTag();

    Response<Object> getTagAndPosts(String tagLabel, Integer currentPage);

    Response<List<Tag>> updateTag(Tag tag);

    Response<List<Tag>> deleteTag(Tag tag);

    Response<List<Tag>> forceDeleteTag(Tag tag);
}
