package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.Response;
import com.flip.entity.TagOption;

import java.util.List;

public interface TagOptionService extends IService<TagOption> {
    Response<TagOption> addTagOption(TagOption tagOption);

    Response<List<TagOption>> getAllTagOptions();

    Response<TagOption> updateTagOption(TagOption tagOption);

    Response<List<TagOption>> deleteTagOption(TagOption tagOption);

    Response<Object> forceDeleteTagOption(TagOption tagOption);

    Response<Object> getTagsAndOptions();
}
