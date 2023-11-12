package com.flip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flip.domain.entity.TagOption;

import java.util.List;
import java.util.Map;

public interface TagOptionService extends IService<TagOption> {

    List<TagOption> getAllTagOptions();

    Map<String, Object> getTagsAndOptions();

}
